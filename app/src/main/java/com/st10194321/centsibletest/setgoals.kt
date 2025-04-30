package com.st10194321.centsibletest

import android.content.Intent
import android.health.connect.datatypes.ExercisePerformanceGoal.AmrapGoal
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.st10194321.centsibletest.databinding.ActivityEditprofileBinding
import com.st10194321.centsibletest.databinding.ActivityProfileBinding
import com.st10194321.centsibletest.databinding.ActivitySetgoalsBinding


class setgoals : AppCompatActivity() {

    private lateinit var binding: ActivitySetgoalsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetgoalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.saveButton.setOnClickListener {
            val month = binding.monthSpinner.selectedItem.toString()
            val minGoal = binding.minGoalEdit.text.toString().toIntOrNull()
            val maxGoal = binding.maxGoalEdit.text.toString().toIntOrNull()

            if (minGoal == null || maxGoal == null) {
                Toast.makeText(this, "Please enter valid numbers.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                Toast.makeText(this, "User not signed in.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = FirebaseFirestore.getInstance()
            val goalsRef = db.collection("users").document(uid).collection("goals")

            // First check if a goal already exists for this month
            goalsRef.whereEqualTo("month", month).get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        Toast.makeText(this, "You already set a goal for $month.", Toast.LENGTH_LONG).show()
                    } else {
                        // Add new goal
                        val goalData = mapOf(
                            "month" to month,
                            "minimumgoal" to minGoal,
                            "maximumgoal" to maxGoal
                        )
                        goalsRef.add(goalData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Goal saved for $month", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error checking goals: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
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
    }
}
