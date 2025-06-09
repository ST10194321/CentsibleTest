package com.st10194321.centsibletest

object FinancialHealthCalculator {
    fun percentSaved(income: Double, spend: Double): Double =
        ((income - spend) / income * 100).coerceIn(0.0, 100.0)

    fun healthScore(percentSaved: Double): Int = when {
        percentSaved >= 50 -> 90 + ((percentSaved - 50) / 50 * 10).toInt()  // 90–100
        percentSaved >= 20 -> 60 + ((percentSaved - 20) / 30 * 30).toInt()  // 60–90
        else               -> (percentSaved / 20 * 60).toInt()              // 0–60
    }
}

//Author: GeeksforGeeks
//Accessibiltiy: https://www.geeksforgeeks.org/how-to-make-a-scientific-calculator-android-app-using-kotlin/
//Date Accessed: 05/06/2025

