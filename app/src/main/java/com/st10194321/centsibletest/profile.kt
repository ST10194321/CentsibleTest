package com.st10194321.centsibletest

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.st10194321.centsibletest.databinding.ActivityMainBinding
import com.st10194321.centsibletest.databinding.ActivityProfileBinding

class profile : AppCompatActivity() {


    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Initialize views


        // Handle Edit Profile click
        binding.tvEditProfile.setOnClickListener {

            val i = Intent(this, editprofile::class.java)
            startActivity(i)

        }

        // Handle Set Goals click
        binding.cardSetGoals.setOnClickListener {
            startActivity(Intent(this, setgoals::class.java))
        }

        // Handle Set Income click
        binding.cardSetIncome.setOnClickListener {
            startActivity(Intent(this, setincome::class.java))
        }
    }
}
