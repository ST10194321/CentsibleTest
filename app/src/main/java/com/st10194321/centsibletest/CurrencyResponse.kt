package com.st10194321.centsibletest

import com.google.gson.annotations.SerializedName

data class CurrencyResponse(
    val date: String,

    @SerializedName("zar")
    val quotes: Map<String, Double>
)
