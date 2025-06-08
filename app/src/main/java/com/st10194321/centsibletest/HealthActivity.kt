package com.st10194321.centsibletest

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.st10194321.centsibletest.databinding.ActivityHealthBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HealthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHealthBinding
    // match your viewTrans date format
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHealthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
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


        loadFinancialHealth()

    }


    private fun loadFinancialHealth() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return toast("Not logged in")
        val db = FirebaseFirestore.getInstance()

        // 1) Load user income
        db.collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->
                val income = userDoc.getDouble("monthlyIncome") ?: 0.0
                if (income <= 0.0) {
                    return@addOnSuccessListener toast("Please set your income first")
                }

                // 2) Fetch transactions under users/{uid}/transactions
                db.collection("users")
                    .document(uid)
                    .collection("transactions")
                    .get()
                    .addOnSuccessListener { snap ->
                        // parse (date, amount) pairs
                        val entries = snap.documents.mapNotNull { doc ->
                            val dateStr = doc.getString("date") ?: return@mapNotNull null
                            val date = try { sdf.parse(dateStr) } catch (_: Exception) { null }
                            val amt  = doc.getDouble("amount")
                            if (date != null && amt != null) Pair(date, amt) else null
                        }

                        // 3) Define month boundaries
                        val calThis = Calendar.getInstance().apply {
                            set(Calendar.DAY_OF_MONTH, 1)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val startThis = calThis.time
                        val calLast = calThis.clone() as Calendar
                        calLast.add(Calendar.MONTH, -1)
                        val startLast = calLast.time

                        // 4) Sum spends
                        val thisMonthSpend = entries
                            .filter { it.first >= startThis }
                            .sumOf { it.second }
                        val lastMonthSpend = entries
                            .filter { it.first >= startLast && it.first < startThis }
                            .sumOf { it.second }

                        // 5) Compute metrics
                        val pctUsed      = (thisMonthSpend / income * 100).coerceAtMost(100.0)
                        val pctSaved     = FinancialHealthCalculator.percentSaved(income, thisMonthSpend)
                        val score        = FinancialHealthCalculator.healthScore(pctSaved)
                        val momPctChange = if (lastMonthSpend > 0)
                            (thisMonthSpend - lastMonthSpend) / lastMonthSpend * 100
                        else null

                        val today       = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                        val daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
                        val daysLeft    = daysInMonth - today
                        val dailyBud    = if (daysLeft > 0) (income - thisMonthSpend) / daysLeft else null

                        // no per-category budgets yet:
                        val signals = TipEngine.Signals(
                            pctUsed = pctUsed,
                            pctSaved = pctSaved,
                            categoryOverspend = emptyMap(),
                            momPctChange = momPctChange,
                            dailyBudget = dailyBud,
                            nOverspentCats = 0
                        )

                        // 6) Bind key metrics and health gauge
                        binding.tvSavingsRate.text = "${"%.0f".format(pctSaved)}%"
                        binding.tvBudgetUsed.text  = "${"%.0f".format(pctUsed)}%"
                        binding.healthProgress.progress = score
                        binding.tvHealthScore.text      = "Score: $score/100"

                        // 7) Generate & show tips
                        val rawTips = TipEngine.generateTips(signals)
                        val tipItems = rawTips.map { msg -> Tip(R.drawable.ic_lightbulb, msg) }
                        binding.tipsRecyclerView.apply {
                            layoutManager = LinearLayoutManager(this@HealthActivity)
                            adapter       = TipAdapter(tipItems)
                        }
                    }
                    .addOnFailureListener { e ->
                        toast("Error loading transactions: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                toast("Error loading profile: ${e.message}")
            }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
