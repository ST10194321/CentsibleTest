package com.st10194321.centsibletest

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url


interface CurrencyApiService {
    @GET
    suspend fun getRatesByUrl(@Url fullUrl: String): Response<CurrencyResponse>
}
