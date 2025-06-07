package com.st10194321.centsibletest
import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AchievementManager(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val uid = auth.currentUser?.uid

    /**
     * Unlocks an achievement for the current user, if not already unlocked.
     */

    private fun awardAchievement(context: Context, uid: String, name: String) {
        val achievementRef = db.collection("users").document(uid)
            .collection("achievements").document(name)

        achievementRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val achievement = hashMapOf(
                    "name" to name,
                    "timestamp" to System.currentTimeMillis()
                )
                achievementRef.set(achievement)
                Toast.makeText(context, "Achievement unlocked: $name 🏆", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun checkAllAchievements(context: Context, uid: String) {
        checkForFirstSteps(context, uid)
        checkBudgetBeginner(context, uid)
        checkSmartSpender(context, uid)
    }

    fun checkForFirstSteps(context: Context, uid: String) {
        db.collection("users").document(uid)
            .collection("goals")
            .get()
            .addOnSuccessListener { goalsSnap ->
                if (!goalsSnap.isEmpty) {
                    awardAchievement(context, uid, "First Steps")
                }
            }
    }

        // 🥈 Budget Beginner – Complete one month within your max goal
        fun checkBudgetBeginner(context : Context,uid: String) {


            db.collection("users").document(uid)
                .collection("goals")
                .get()
                .addOnSuccessListener { goalsSnap ->
                    val monthNames = Calendar.getInstance().let { java.text.DateFormatSymbols().months }

                    for (goalDoc in goalsSnap.documents) {
                        val month = goalDoc.getString("month") ?: continue
                        val maxGoal = goalDoc.getLong("maximumgoal")?.toDouble() ?: continue

                        db.collection("users").document(uid)
                            .collection("transactions")
                            .get()
                            .addOnSuccessListener { txSnap ->
                                var spent = 0.0
                                for (tx in txSnap) {
                                    val dateString = tx.getString("date") ?: continue
                                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    val cal = Calendar.getInstance()
                                    try {
                                        cal.time = sdf.parse(dateString) ?: continue
                                    } catch (e: Exception) {
                                        continue
                                    }

                                    val txMonth = monthNames[cal.get(Calendar.MONTH)]
                                    if (txMonth.equals(month, ignoreCase = true)) {
                                        spent += tx.getDouble("amount") ?: 0.0
                                    }
                                }


                                if (spent <= maxGoal) {
                                    awardAchievement(context, uid, "Budget Beginner")
                                }
                            }
                    }
                }
        }

        // 🥉 Smart Spender – Spend less than your minimum goal
        fun checkSmartSpender(context : Context,uid: String) {
            val monthNames = DateFormatSymbols().months

            // Step 1: Load all goals
            db.collection("users").document(uid)
                .collection("goals")
                .get()
                .addOnSuccessListener { goalsSnap ->
                    for (g in goalsSnap.documents) {
                        val month = g.getString("month") ?: continue
                        val minGoal = (g.getLong("minimumgoal") ?: 0L).toDouble()

                        // Step 2: Load all transactions
                        db.collection("users").document(uid)
                            .collection("transactions")
                            .get()
                            .addOnSuccessListener { txSnap ->
                                var spent = 0.0
                                for (tx in txSnap.documents) {
                                    val dateStr = tx.getString("date") ?: continue

                                    // Parse the date string to get the transaction's month
                                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    val cal = Calendar.getInstance()
                                    try {
                                        cal.time = sdf.parse(dateStr) ?: continue
                                    } catch (e: Exception) {
                                        continue
                                    }

                                    val txMonth = monthNames[cal.get(Calendar.MONTH)]

                                    // If the transaction is in the same month as the goal
                                    if (txMonth.equals(month, ignoreCase = true)) {
                                        spent += tx.getDouble("amount") ?: 0.0
                                    }
                                }

                                // Step 3: If user spent less than min goal, award the achievement
                                if (spent < minGoal) {
                                    awardAchievement(context,uid, "Smart Spender")
                                }
                            }
                    }
                }
        }

}

