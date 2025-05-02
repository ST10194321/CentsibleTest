package com.st10194321.centsibletest

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

//category model
data class Category(
    val name: String       = "",
    val type: String       = "",
    val details: String    = "",
    val amount: Long       = 0L,
    val occurrence: String = "",
    @ServerTimestamp
    val createdAt: Date?   = null
)
