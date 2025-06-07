package com.st10194321.centsibletest

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CurrencyRepository private constructor(private val prefs: SharedPreferences) {

    companion object {
        private const val PREFS_KEY = "centsible_currency_prefs"
        private const val KEY_LAST_FETCH_TIME = "last_fetch_time"   // in millis
        private const val KEY_CACHED_RATES = "cached_rates_json"    // JSON‐string

        private const val JSDELIVR_BASE = "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/"
        private const val PAGES_DEV_BASE = "https://latest.currency-api.pages.dev/v1/"

        @Volatile
        private var instance: CurrencyRepository? = null


        fun initialize(context: Context) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
                        instance = CurrencyRepository(prefs)
                    }
                }
            }
        }


        fun get(): CurrencyRepository {
            return instance
                ?: throw IllegalStateException("CurrencyRepository not initialized. Call initialize() first.")
        }
    }

    // Retrofit with dummy base (we’ll override via @Url)
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://example.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service: CurrencyApiService = retrofit.create(CurrencyApiService::class.java)


    suspend fun fetchLatestRates(): Map<String, Double>? = withContext(Dispatchers.IO) {
        val primaryUrl = JSDELIVR_BASE + "currencies/zar.json"
        val fallbackUrl = PAGES_DEV_BASE + "currencies/zar.json"

        // 1) Try jsDelivr
        try {
            val response = service.getRatesByUrl(primaryUrl)
            if (response.isSuccessful) {
                response.body()?.let { resp ->
                    cacheRates(resp)
                    return@withContext resp.quotes
                }
            }
        } catch (_: Exception) {
            // ignore, proceed to fallback
        }

        // 2) Try Cloudflare fallback
        try {
            val response2 = service.getRatesByUrl(fallbackUrl)
            if (response2.isSuccessful) {
                response2.body()?.let { resp2 ->
                    cacheRates(resp2)
                    return@withContext resp2.quotes
                }
            }
        } catch (_: Exception) {
            // ignore
        }

        // 3) Both failed → return cached if available
        return@withContext getCachedRatesOrNull()
    }

    /** Write the quotes map to SharedPreferences as JSON + record timestamp. */
    private fun cacheRates(resp: CurrencyResponse) {
        val editor = prefs.edit()
        val quotesJson = Gson().toJson(resp.quotes)
        editor.putString(KEY_CACHED_RATES, quotesJson)
        editor.putLong(KEY_LAST_FETCH_TIME, System.currentTimeMillis())
        editor.apply()
    }

    /** Return the cached map, or null if never saved. */
    private fun getCachedRatesOrNull(): Map<String, Double>? {
        val json = prefs.getString(KEY_CACHED_RATES, null) ?: return null
        val type = object : TypeToken<Map<String, Double>>() {}.type
        return Gson().fromJson(json, type)
    }

    /**
     * Return the cached rates **only if** they are younger than maxAgeMillis (default = 12h).
     * Otherwise return null, forcing a re-fetch.
     */
    fun getRatesIfFreshOrNull(maxAgeMillis: Long = 12 * 60 * 60 * 1000): Map<String, Double>? {
        val last = prefs.getLong(KEY_LAST_FETCH_TIME, 0L)
        val now = System.currentTimeMillis()
        return if (prefs.contains(KEY_CACHED_RATES) && (now - last) < maxAgeMillis) {
            getCachedRatesOrNull()
        } else {
            null
        }
    }
}
