package com.st10194321.centsibletest

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.st10194321.centsibletest.databinding.ActivityEditprofileBinding
import com.st10194321.centsibletest.databinding.ActivityProfileBinding
import com.st10194321.centsibletest.databinding.ActivitySetgoalsBinding

class setgoals : AppCompatActivity() {
    private lateinit var binding: ActivitySetgoalsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_setgoals)

        binding = ActivitySetgoalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.iconHome.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }
        binding.iconProfile.setOnClickListener {
            val i = Intent(this, profile::class.java)
            startActivity(i)
            finish()
        }
    }
}