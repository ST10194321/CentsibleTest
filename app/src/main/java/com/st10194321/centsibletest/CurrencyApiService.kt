package com.st10194321.centsibletest

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

// Currency API Service
interface CurrencyApiService {
    @GET
    suspend fun getRatesByUrl(@Url fullUrl: String): Response<CurrencyResponse>
}


//Author: Android Developers
//Accessibiltiy: https://developer.android.com/reference/android/icu/util/Currency
//Date Accessed: 09/06/2025