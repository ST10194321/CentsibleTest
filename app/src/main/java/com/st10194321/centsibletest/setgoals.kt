package com.st10194321.centsibletest

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.st10194321.centsibletest.databinding.ActivitySetgoalsBinding


class setgoals : AppCompatActivity() {

    private lateinit var binding: ActivitySetgoalsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetgoalsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        // Save goal to Firestore
        binding.saveButton.setOnClickListener {
            val month = binding.monthSpinner.selectedItem.toString()
            val minGoal = binding.minGoalEdit.text.toString().toIntOrNull()
            val maxGoal = binding.maxGoalEdit.text.toString().toIntOrNull()
            val achievementManager = AchievementManager(this)

            // Validates input
            if (minGoal == null || maxGoal == null) {
                Toast.makeText(this, "Please enter valid numbers.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                achievementManager.checkForFirstSteps(this, uid)
            }
            if (uid == null) {
                Toast.makeText(this, "User not signed in.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = FirebaseFirestore.getInstance()
            val goalsRef = db.collection("users").document(uid).collection("goals")

            // Prevents duplicate goal entries for the same month
            goalsRef.whereEqualTo("month", month).get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        Toast.makeText(this, "You already set a goal for $month.", Toast.LENGTH_LONG).show()
                    } else {
                        val goalData = mapOf(
                            "month" to month,
                            "minimumgoal" to minGoal,
                            "maximumgoal" to maxGoal
                        )
                        goalsRef.add(goalData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Goal saved for $month", Toast.LENGTH_SHORT).show()
                                val i = Intent(this, viewgoals::class.java)
                                startActivity(i)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error checking goals: ${e.message}", Toast.LENGTH_LONG).show()
                }
            //Author: John Cowan
            //Accessibiltiy: https://stackoverflow.com/questions/65556362/android-kotlin-get-value-of-selected-spinner-item
            //Date Accessed: 24/04/2025
        }

        // Navigation Bar
        binding.iconHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.iconProfile.setOnClickListener {
            startActivity(Intent(this, profile::class.java))
            finish()
        }

        binding.iconCategories.setOnClickListener {
            startActivity(Intent(this, viewBugCat::class.java))
            finish()
        }

        // Back button
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
    }
}

