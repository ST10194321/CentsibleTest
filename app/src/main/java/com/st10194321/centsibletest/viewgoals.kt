package com.st10194321.centsibletest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.st10194321.centsibletest.databinding.ActivityViewgoalsBinding
import java.text.DateFormatSymbols
import java.util.Calendar

class viewgoals : AppCompatActivity() {
    private lateinit var binding: ActivityViewgoalsBinding
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewgoalsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        // Navigate to set goals screen when button is clicked
        binding.setGoalButton.setOnClickListener {
            startActivity(Intent(this, setgoals::class.java))
        }

        // Load the user's goals and calculate how much has been spent
        loadGoals()

        // Navigation bar
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

        // Back arrow action
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    // Load goals and calculate transaction data per month
    private fun loadGoals() {
        val uid = auth.currentUser?.uid ?: return
        val monthNames = DateFormatSymbols().months  // Full month names, e.g., "January", "February"

        // Fetch all goal documents for this user
        db.collection("users").document(uid)
            .collection("goals")
            .get()
            .addOnSuccessListener { goalsSnap ->
                for (g in goalsSnap.documents) {
                    val month   = g.getString("month") ?: continue
                    val minGoal = (g.getLong("minimumgoal") ?: 0L).toDouble()
                    val maxGoal = (g.getLong("maximumgoal") ?: 0L).toDouble()

                    // Inflate a new goal card layout for each goal
                    val card = layoutInflater.inflate(
                        R.layout.goal_card,
                        binding.goalsContainer,
                        false
                    )

                    // Get references to views inside the goal card
                    val tvMonth  = card.findViewById<TextView>(R.id.tvMonth)
                    val tvMin    = card.findViewById<TextView>(R.id.tvMinGoal)
                    val tvMax    = card.findViewById<TextView>(R.id.tvMaxGoal)
                    val pb       = card.findViewById<ProgressBar>(R.id.pbGoalProgress)
                    val tvSpent  = card.findViewById<TextView>(R.id.tvSpent)
                    val tvLeft   = card.findViewById<TextView>(R.id.tvLeft)

                    // Set the goal info into the UI
                    tvMonth.text  = month
                    tvMin.text    = "Min Goal: R%.2f".format(minGoal)
                    tvMax.text    = "Max Goal: R%.2f".format(maxGoal)
                    tvSpent.text  = "Spent: R0.00"  // Default until loaded
                    tvLeft.text   = "Left: R%.2f".format(maxGoal)  // Initially assume none spent
                    pb.progress   = 0  // Progress bar is empty initially

                    // Add the goal card to the container
                    binding.goalsContainer.addView(card)

                    // Add spacing below each card
                    binding.goalsContainer.addView(View(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            resources.getDimensionPixelSize(R.dimen.card_gap)
                        )
                    })

                    // Fetch transactions and calculate spent amount for this goal's month
                    db.collection("users").document(uid)
                        .collection("transactions")
                        .get()
                        .addOnSuccessListener { txSnap ->
                            var spent = 0.0

                            // Loop through each transaction to match with current goal's month
                            for (tx in txSnap.documents) {
                                val tsMillis = tx.getLong("timestamp") ?: continue
                                val cal = Calendar.getInstance().apply {
                                    timeInMillis = tsMillis
                                }
                                val txMonth = monthNames[cal.get(Calendar.MONTH)]
                                if (txMonth.equals(month, ignoreCase = true)) {
                                    spent += tx.getDouble("amount") ?: 0.0
                                }
                            }

                            // Calculate remaining budget and percentage spent
                            val left = (maxGoal - spent).coerceAtLeast(0.0)
                            val pct  = if (maxGoal == 0.0) 0
                            else ((spent / maxGoal * 100).coerceIn(0.0, 100.0)).toInt()

                            // Update UI with calculated values
                            tvSpent.text = "Spent: R%.2f".format(spent)
                            tvLeft.text  = "Left: R%.2f".format(left)
                            pb.progress  = pct
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Error loading transactions: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error loading goals: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}

