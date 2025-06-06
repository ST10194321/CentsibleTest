package com.st10194321.centsibletest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DateFormatSymbols
import java.util.Calendar

import com.st10194321.centsibletest.formatInSelectedCurrency

class viewTrans : AppCompatActivity() {

    private lateinit var rvTransactions: RecyclerView
    private lateinit var spinnerMonth: Spinner
    private lateinit var tvOverallLabel: TextView
    private lateinit var tvMonthLabel: TextView
    private lateinit var tvRemaining: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var pbBalance: ProgressBar
    private lateinit var btnAddTxn: Button

    private var selectedMonthIndex = 0
    private lateinit var categoryName: String

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // (If you haven’t initialized CurrencyRepository elsewhere, do it here:)
        // CurrencyRepository.initialize(applicationContext)

        enableEdgeToEdge()
        setContentView(R.layout.activity_view_trans)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        // bind XML views
        rvTransactions    = findViewById(R.id.rvTransactions)
        spinnerMonth      = findViewById(R.id.spinnerMonthFilter)
        tvOverallLabel    = findViewById(R.id.tvOverallLabel)
        tvMonthLabel      = findViewById(R.id.tvMonthLabel)
        tvRemaining       = findViewById(R.id.tvRemaining)
        tvTotalAmount     = findViewById(R.id.tvTotalAmount)
        pbBalance         = findViewById(R.id.pbBalance)
        btnAddTxn         = findViewById(R.id.btnAddTxn)

        // get category name from intent
        categoryName = intent.getStringExtra(EXTRA_CATEGORY) ?: run {
            Toast.makeText(this, "No category specified", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // set default labels
        tvOverallLabel.text = categoryName
        tvMonthLabel.text =
            DateFormatSymbols().months[Calendar.getInstance().get(Calendar.MONTH)]

        // populate month filter spinner
        val months = resources.getStringArray(R.array.month_filter_entries)
        val monthAdapter = ArrayAdapter(
            this,
            R.layout.spinner_item,
            months
        ).also { it.setDropDownViewResource(R.layout.spinner_dropdown_item) }
        spinnerMonth.adapter = monthAdapter

        spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, pos: Int, id: Long
            ) {
                selectedMonthIndex = pos

                // update the month shown in the card
                val monthNames = resources.getStringArray(R.array.month_filter_entries)
                tvMonthLabel.text = if (pos == 0) "All" else monthNames[pos]

                loadCategoryLimit(categoryName)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // “add transaction” button
        btnAddTxn.setOnClickListener {
            startActivity(Intent(this, add_trans::class.java))
            finish()
        }
    }

    // retrieves category limit (in ZAR) and then fetches transactions
    private fun loadCategoryLimit(categoryName: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(uid)
            .collection("categories")
            .whereEqualTo("name", categoryName)
            .limit(1)
            .get()
            .addOnSuccessListener { snap ->
                val limit = snap.documents.firstOrNull()?.getLong("amount") ?: 0L
                fetchTransactions(categoryName, limit.toDouble())
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error loading limit: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    // pulls transactions from Firestore to show in RecyclerView
    private fun fetchTransactions(categoryName: String, limitInZar: Double) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(uid)
            .collection("transactions")
            .whereEqualTo("category", categoryName)
            .get()
            .addOnSuccessListener { docs ->
                val list = mutableListOf<Transaction>()
                var totalSpentZar = 0.0

                for (doc in docs) {
                    val name    = doc.getString("name") ?: ""
                    val amountZar  = doc.getDouble("amount") ?: 0.0
                    val details = doc.getString("details") ?: ""
                    val date    = doc.getString("date") ?: ""
                    val image   = doc.getString("image") ?: ""

                    val month = date.split("/").getOrNull(1)?.toIntOrNull() ?: 0

                    if (selectedMonthIndex == 0 || month == selectedMonthIndex) {
                        totalSpentZar += amountZar
                        list.add(Transaction(name, amountZar, details, date, image))
                    }
                }

                // Calculate remaining in ZAR
                val remainingZar = (limitInZar - totalSpentZar).coerceAtLeast(0.0)

                // NOW convert to selected currency for display:
                tvRemaining.text   = formatInSelectedCurrency(remainingZar)
                tvTotalAmount.text = formatInSelectedCurrency(totalSpentZar)

                val pct = if (limitInZar == 0.0) 0
                else ((totalSpentZar / limitInZar * 100).coerceIn(0.0, 100.0)).toInt()
                pbBalance.progress = pct

                // show filtered list
                rvTransactions.layoutManager = LinearLayoutManager(this)
                rvTransactions.adapter = TransactionAdapter(list) { txn ->
                    startActivity(Intent(this, TransactionDetailActivity::class.java).apply {
                        putExtra("title", txn.name)
                        putExtra("amount", txn.amount.toString())
                        putExtra("details", txn.details)
                        putExtra("date", txn.date)
                        putExtra("image", txn.image)
                    })
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error fetching transactions: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

        // back button
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
    }

    companion object {
        const val EXTRA_CATEGORY = "EXTRA_CATEGORY"
    }
}
