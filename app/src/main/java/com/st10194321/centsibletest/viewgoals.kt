package com.st10194321.centsibletest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.st10194321.centsibletest.databinding.ActivityProfileBinding
import com.st10194321.centsibletest.databinding.ActivityViewgoalsBinding

class viewgoals : AppCompatActivity() {
    private lateinit var binding: ActivityViewgoalsBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewgoalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navigate to the set goals screen
        binding.setGoalButton.setOnClickListener {
            val intent = Intent(this, setgoals::class.java)
            startActivity(intent)
        }

        // Load goals for the current user
        loadGoals()

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



    private fun loadGoals() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).collection("goals")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val month = doc.getString("month") ?: continue
                    val minGoal = doc.getDouble("minimumgoal") ?: 0.0
                    val maxGoal = doc.getDouble("maximumgoal") ?: 0.0

                    // Inflate the card layout dynamically
                    val card = layoutInflater.inflate(R.layout.goal_card, null)

                    val tvMonth = card.findViewById<TextView>(R.id.tvMonth)
                    val tvMin = card.findViewById<TextView>(R.id.tvMinGoal)
                    val tvMax = card.findViewById<TextView>(R.id.tvMaxGoal)

                    tvMonth.text = month
                    tvMin.text = "Min Goal: R${minGoal.toInt()}"
                    tvMax.text = "Max Goal: R${maxGoal.toInt()}"

                    // Add the card to the container
                    binding.goalsContainer.addView(card)

                    // Add a space between cards
                    val spacer = View(this)
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        resources.getDimensionPixelSize(R.dimen.card_gap)  // This should be defined in dimens.xml
                    )
                    spacer.layoutParams = params
                    binding.goalsContainer.addView(spacer)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load goals", Toast.LENGTH_SHORT).show()
            }

    }

}

