package com.st10194321.centsibletest

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.st10194321.centsibletest.databinding.ActivityAchievementsBinding

class achievements : AppCompatActivity() {
    private lateinit var binding: ActivityAchievementsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_achievements)

        // Navigate to Home screen
        binding.iconHome.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }

        // Navigate to Categories screen
        binding.iconCategories.setOnClickListener {
            val i = Intent(this, viewBugCat::class.java)
            startActivity(i)
            finish()
        }

        // Navigate to Profile screen
        binding.iconProfile.setOnClickListener {
            val i = Intent(this, profile::class.java)
            startActivity(i)
            finish()
        }
    }
}
