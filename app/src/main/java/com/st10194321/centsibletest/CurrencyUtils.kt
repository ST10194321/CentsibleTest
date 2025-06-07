package com.st10194321.centsibletest

import android.content.Context
import kotlinx.coroutines.runBlocking
import java.text.NumberFormat
import java.util.Locale


fun Context.formatInSelectedCurrency(amountInZar: Double): String {
    val prefs = this.getSharedPreferences("centsible_prefs", Context.MODE_PRIVATE)
    val chosenCurrency = prefs.getString("selected_currency", "ZAR") ?: "ZAR"

    val repo = CurrencyRepository.get()
    // 1) Try cached if <12h old
    val ratesMap = repo.getRatesIfFreshOrNull()
        ?: runBlocking {
            // Blocking fetch if cache is invalid or missing
            repo.fetchLatestRates()
        }

    // 2) Determine rate
    val rate = if (chosenCurrency == "ZAR") 1.0
    else ratesMap?.get(chosenCurrency.lowercase()) ?: 1.0

    val converted = amountInZar * rate

    // 3) Pick a locale for formatting
    val locale = when (chosenCurrency) {
        "USD" -> Locale.US
        "EUR" -> Locale("de", "DE")    // or Locale.FRANCE
        "GBP" -> Locale.UK
        else  -> Locale("en", "ZA")
    }
    val fmt = NumberFormat.getCurrencyInstance(locale)
    return fmt.format(converted)
}
