package com.st10194321.centsibletest

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

//RoomDB
@Dao
interface userDao {
    // add or update user
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: users)

    // fetch user by email
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): users?
}

//Author: Android Developers
//Accessibiltiy: https://developer.android.com/training/data-storage/room/accessing-data#kotlin
//Date Accessed: 02/05/2025