package com.st10194321.centsibletest

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class users(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val password: String,

    )