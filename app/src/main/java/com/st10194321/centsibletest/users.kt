package com.st10194321.centsibletest

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class users(
    // auto‑generated ID
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    // user email
    val email: String,
    // hashed password
    val password: String,

    )
//Author: Android Developers
//Accessibiltiy: https://developer.android.com/training/data-storage/room/accessing-data#kotlin
//Date Accessed: 02/05/2025