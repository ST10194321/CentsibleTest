package com.st10194321.centsibletest

import TransactionAdapter
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

//Author: John Cowan
//Accessibiltiy: https://stackoverflow.com/questions/65556362/android-kotlin-get-value-of-selected-spinner-item
//Date Accessed: 24/04/2025

        // spinner selection listener
        spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, pos: Int, id: Long
            ) {
                selectedMonthIndex = pos

                //update the month shown in the card
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

    //retrieves category limit
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
                fetchTransactions(categoryName, limit)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error loading limit: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
// pulls transactions from firestore to show in recycler view
    private fun fetchTransactions(categoryName: String, limit: Long) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(uid)
            .collection("transactions")
            .whereEqualTo("category", categoryName)
            .get()
            .addOnSuccessListener { docs ->
                val list = mutableListOf<Transaction>()
                var total = 0.0

                for (doc in docs) {
                    val name    = doc.getString("name") ?: ""
                    val amount  = doc.getDouble("amount") ?: 0.0
                    val details = doc.getString("details") ?: ""
                    val date    = doc.getString("date") ?: ""
                    val image   = doc.getString("image") ?: ""

                    val month = date.split("/").getOrNull(1)?.toIntOrNull() ?: 0

                    if (selectedMonthIndex == 0 || month == selectedMonthIndex) {
                        total += amount
                        list.add(Transaction(name, amount, details, date, image))
                    }
                }

                // update UI
                val remaining = (limit - total).coerceAtLeast(0.0)
                tvRemaining.text   = "R%.2f".format(remaining)
                tvTotalAmount.text = "R%.2f".format(total)
                val pct = if (limit == 0L) 0
                else ((total / limit * 100).coerceIn(0.0, 100.0)).toInt()
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
    //Author: Firebase Documentation Team
  //Accessibiltiy: https://firebase.google.com/docs/firestore/query-data/get-data#custom_objects
  //Date Accessed: 24/04/2025

    //back button
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
    }

    companion object {
        const val EXTRA_CATEGORY = "EXTRA_CATEGORY"
    }
}
