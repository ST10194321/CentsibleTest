package com.st10194321.centsibletest

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class profile : AppCompatActivity() {

    private lateinit var cdEditProfile: CardView
    private lateinit var cdSetGoals: CardView
    private lateinit var cdSetIncome: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        cdEditProfile = findViewById(R.id.cardEditProfile)
        cdSetGoals = findViewById(R.id.cardSetGoals)
        cdSetIncome = findViewById(R.id.cardSetIncome)

        cdEditProfile.setOnClickListener {
            // Handle Edit Profile button click
            val i = android.content.Intent(this, editprofile::class.java)
            startActivity(i)
        }

        cdSetGoals.setOnClickListener {
            // Handle Set Goals button click
            val i = android.content.Intent(this, setgoals::class.java)
            startActivity(i)
        }

        cdSetIncome.setOnClickListener {
            // Handle Set Income button click
            val i = android.content.Intent(this, setincome::class.java)
            startActivity(i)
        }
    }
}