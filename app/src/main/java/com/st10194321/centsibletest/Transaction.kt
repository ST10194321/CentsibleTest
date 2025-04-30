package com.st10194321.centsibletest

import kotlin.io.encoding.Base64

//class Transaction {
//
//}

//data class Transaction(val name: String, val amount: Double)


data class Transaction(
    val name: String,
    val amount: Double,
    val details: String,
    val date: String, // You could use `LocalDate` or `Date` if preferred
    val image: String
)
