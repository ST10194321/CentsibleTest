package com.st10194321.centsibletest

//class Transaction {
//
//}

//data class Transaction(val name: String, val amount: Double)


data class Transaction(
    val name: String,
    val amount: Double,
    val date: String // You could use `LocalDate` or `Date` if preferred
)
