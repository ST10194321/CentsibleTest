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

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tvTotalSpent: TextView
    private lateinit var tvTotalLeft: TextView
    private lateinit var pbBalance: ProgressBar

    // Firebase
    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        ViewCompat.setOnApplyWindowInsetsListener(binding.homeLayout) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }


        //binding for xml elements
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

        //nav bar buttons
        binding.iconHome.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }
        //nav bar buttons
        binding.iconProfile.setOnClickListener {
            val i = Intent(this, profile::class.java)
            startActivity(i)
            finish()
        }
        //nav bar buttons
        binding.iconCategories.setOnClickListener {
            val i = Intent(this, viewBugCat::class.java)
            startActivity(i)
            finish()
        }

    }


    private fun loadOverallStats() {
        val uid = auth.currentUser?.uid ?: return

        //  sum all category limits
        db.collection("users").document(uid)
            .collection("categories")
            .get()
            .addOnSuccessListener { catSnap ->
                var totalLimit = 0L
                catSnap.documents.forEach { totalLimit += it.getLong("amount") ?: 0L }

                // sum all transaction amounts
                db.collection("users").document(uid)
                    .collection("transactions")
                    .get()
                    .addOnSuccessListener { txSnap ->
                        var totalSpent = 0.0
                        txSnap.documents.forEach {
                            totalSpent += it.getDouble("amount") ?: 0.0
                        }


                        val totalLeft = (totalLimit - totalSpent).coerceAtLeast(0.0)
                        tvTotalSpent.text = "R%.2f".format(totalSpent)
                        tvTotalLeft.text  = "R%.2f".format(totalLeft)

                        val pct = if (totalLimit == 0L) 0
                        else ((totalSpent / totalLimit * 100)
                            .coerceIn(0.0, 100.0)).toInt()
                        pbBalance.progress = pct
                    }
            }
    }
}