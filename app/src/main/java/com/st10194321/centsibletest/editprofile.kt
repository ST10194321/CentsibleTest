package com.st10194321.centsibletest

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.st10194321.centsibletest.databinding.ActivityEditprofileBinding

class editprofile : AppCompatActivity() {

    private lateinit var binding: ActivityEditprofileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up view binding
        binding = ActivityEditprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val uid = auth.currentUser?.uid

        // Load current user data into fields
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        binding.editFirstName.setText(document.getString("firstName") ?: "")
                        binding.editLastName.setText(document.getString("lastName") ?: "")
                        binding.editPhoneNumber.setText(document.getString("phone") ?: "")
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to load profile:\n${e.message}", Toast.LENGTH_LONG).show()
                }
        }
        //Author: Firebase Documentation Team
        // Accessibiltiy: https://firebase.google.com/docs/firestore/query-data/get-data#get_a_document
        // Date Accessed: 20/04/2025

        // Save updated user info to Firestore
        binding.saveButton.setOnClickListener {
            val updatedProfile = mapOf(
                "firstName" to binding.editFirstName.text.toString(),
                "lastName" to binding.editLastName.text.toString(),
                "phone" to binding.editPhoneNumber.text.toString()
            )

            if (uid != null) {
                db.collection("users").document(uid)
                    .set(updatedProfile, SetOptions.merge())
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, profile::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update profile:\n${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }

        // Navigation bar
        binding.iconHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        binding.iconHome1.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.iconProfile.setOnClickListener {
            startActivity(Intent(this, profile::class.java))
            finish()
        }
        binding.iconProfile1.setOnClickListener {
            startActivity(Intent(this, profile::class.java))
            finish()
        }

        binding.iconCategories.setOnClickListener {
            startActivity(Intent(this, viewBugCat::class.java))
            finish()
        }
        binding.iconCategories1.setOnClickListener {
            startActivity(Intent(this, viewBugCat::class.java))
            finish()
        }
        binding.iconReports.setOnClickListener {
            startActivity(Intent(this, reports::class.java))
            finish()
        }
        binding.iconReports.setOnClickListener {
            startActivity(Intent(this, reports::class.java))
            finish()
        }

        // Back button
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            startActivity(Intent(this, profile::class.java))
            finish()
        }
    }
}


