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
import com.st10194321.centsibletest.databinding.ActivityAchievementsBinding

class achievements : AppCompatActivity() {
    private lateinit var binding: ActivityAchievementsBinding
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

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
    private fun loadAchievements(uid: String) {
        val achievementViews = mapOf(
            "First Steps" to binding.ivFirstSteps,
            "Budget Beginner" to binding.ivBudgetBeginner,
            "Smart Spender" to binding.ivSmartSpender
        )

        db.collection("users").document(uid).collection("achievements")
            .get()
            .addOnSuccessListener { snap ->
                for (doc in snap.documents) {
                    val name = doc.getString("name") ?: continue
                    achievementViews[name]?.alpha = 1.0f // Highlight unlocked
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Could not load achievements", Toast.LENGTH_SHORT).show()
            }
    }
}
