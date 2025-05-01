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

        binding.iconHome.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }
        // Handle Edit Profile click
        binding.tvEditProfile.setOnClickListener {

            val i = Intent(this, editprofile::class.java)
            startActivity(i)

        }

        // Handle Set Goals click
        binding.cardViewGoals.setOnClickListener {
            startActivity(Intent(this, viewgoals::class.java))
        }

        // Handle Set Income click
        binding.iconHome.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }
//            catsIcon.setOnClickListener {
//                val i = Intent(this, categories::class.java)
//                startActivity(i)
//                finish()
//           }
//            reportsIcon.setOnClickListener {
//                val i = Intent(this, reports::class.java)
//                startActivity(i)
//                finish()
//            }

        binding.iconProfile.setOnClickListener {
            val i = Intent(this, profile::class.java)
            startActivity(i)
            finish()
        }

        binding.cardLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, signin::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }


    }
}
