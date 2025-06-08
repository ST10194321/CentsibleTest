package com.st10194321.centsibletest

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.st10194321.centsibletest.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.st10194321.centsibletest.formatInSelectedCurrency

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tvTotalSpent: TextView
    private lateinit var tvTotalLeft: TextView
    private lateinit var pbBalance: ProgressBar


    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    var aM:AchievementManager = AchievementManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // (If you haven’t already done so in an Application subclass, initialize here:)
        CurrencyRepository.initialize(applicationContext)

        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.homeLayout) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        // bind XML elements
        tvTotalSpent = binding.cardOverall.findViewById(R.id.tvTotalSpent)
        tvTotalLeft  = binding.cardOverall.findViewById(R.id.tvTotalLeft)
        pbBalance    = binding.cardOverall.findViewById(R.id.pbBalance)

        // buttons that take user to specific page
        binding.btnNewBudCat.setOnClickListener {
            startActivity(Intent(this, addBugCat::class.java))
        }
        binding.btnViewBudCat.setOnClickListener {
            startActivity(Intent(this, viewBugCat::class.java))
        }
        binding.btnTrackBudCat.setOnClickListener {
            startActivity(Intent(this, viewgoals::class.java))
        }
        binding.btnAddTrans.setOnClickListener {
            startActivity(Intent(this, add_trans::class.java))
        }

        loadOverallStats()

        // nav bar buttons
        binding.iconHome1.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }
        binding.iconProfile1.setOnClickListener {
            val i = Intent(this, profile::class.java)
            startActivity(i)
            finish()
        }
        binding.iconCategories1.setOnClickListener {
            val i = Intent(this, viewBugCat::class.java)
            startActivity(i)
            finish()
        }
        binding.iconReports1.setOnClickListener {
            val i = Intent(this, reports::class.java)
            startActivity(i)
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
    }

    private fun loadOverallStats() {
        val uid = auth.currentUser?.uid ?: return
        aM.checkBudgetBeginner(this,uid)
        aM.checkSmartSpender(this,uid)

        // sum all category limits (in ZAR)
        db.collection("users").document(uid)
            .collection("categories")
            .get()
            .addOnSuccessListener { catSnap ->
                var totalLimitZar = 0L
                catSnap.documents.forEach { totalLimitZar += it.getLong("amount") ?: 0L }

                // sum all transaction amounts (in ZAR)
                db.collection("users").document(uid)
                    .collection("transactions")
                    .get()
                    .addOnSuccessListener { txSnap ->
                        var totalSpentZar = 0.0
                        txSnap.documents.forEach {
                            totalSpentZar += it.getDouble("amount") ?: 0.0
                        }

                        val totalLeftZar = (totalLimitZar.toDouble() - totalSpentZar).coerceAtLeast(0.0)

                        // NOW convert for display:
                        tvTotalSpent.text = formatInSelectedCurrency(totalSpentZar)
                        tvTotalLeft.text  = formatInSelectedCurrency(totalLeftZar)

                        val pct = if (totalLimitZar == 0L) 0
                        else ((totalSpentZar / totalLimitZar * 100)
                            .coerceIn(0.0, 100.0)).toInt()
                        pbBalance.progress = pct
                    }
            }
    }
}
