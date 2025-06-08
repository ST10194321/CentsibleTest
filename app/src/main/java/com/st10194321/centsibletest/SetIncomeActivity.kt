package com.st10194321.centsibletest

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.st10194321.centsibletest.databinding.ActivitySetIncomeBinding

class SetIncomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetIncomeBinding
    private val auth    by lazy { FirebaseAuth.getInstance() }
    private val db      by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySetIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackUp.setOnClickListener {
            val up = Intent(this, profile::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(up)
            finish()
        }

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
        binding.iconCategories.setOnClickListener {
            val i = Intent(this, viewBugCat::class.java)
            startActivity(i)
            finish()
        }
        binding.iconReports.setOnClickListener {
            val i = Intent(this, reports::class.java)
            startActivity(i)
            finish()
        }
        binding.saveButton.setOnClickListener {
            val incomeStr = binding.etIncome.text.toString().trim()
            val income    = incomeStr.toDoubleOrNull()
            if (income == null || income <= 0) {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save to Firestore under user profile
            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            db.collection("users")
                .document(uid)
                .update("monthlyIncome", income)
                .addOnSuccessListener {
                    Toast.makeText(this, "Income saved!", Toast.LENGTH_SHORT).show()
                    finish()  // go back to dashboard
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving income: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
