package com.st10194321.centsibletest

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

class viewTrans : AppCompatActivity() {
    private lateinit var rvTransactions: RecyclerView
    private lateinit var tvOverallLabel: TextView
    private lateinit var tvMonthLabel: TextView
    private lateinit var tvRemaining: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var pbBalance: ProgressBar
    private lateinit var btnAddTxn: Button

    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView
    private lateinit var btnApplyFilter: Button

    private var startDate: String? = null
    private var endDate: String? = null
    private lateinit var categoryName: String

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val sdf  = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_trans)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        // bind views
        rvTransactions  = findViewById(R.id.rvTransactions)
        tvOverallLabel  = findViewById(R.id.tvOverallLabel)
        tvMonthLabel    = findViewById(R.id.tvMonthLabel)
        tvRemaining     = findViewById(R.id.tvRemaining)
        tvTotalAmount   = findViewById(R.id.tvTotalAmount)
        pbBalance       = findViewById(R.id.pbBalance)
        btnAddTxn       = findViewById(R.id.btnAddTxn)
        tvStartDate     = findViewById(R.id.tvStartDate)
        tvEndDate       = findViewById(R.id.tvEndDate)
        btnApplyFilter  = findViewById(R.id.btnApplyFilter)

        // read category
        categoryName = intent.getStringExtra(EXTRA_CATEGORY) ?: run {
            Toast.makeText(this, "No category specified", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // set labels
        tvOverallLabel.text = if (categoryName == "All Transactions")
            "Overall Budget" else categoryName
        tvMonthLabel.text = DateFormatSymbols().months[Calendar.getInstance().get(Calendar.MONTH)]

        // date pickers
        fun showDatePicker(tv: TextView, isStart: Boolean) {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                val fmt = "%02d/%02d/%04d".format(d, m + 1, y)
                tv.text = fmt
                if (isStart) startDate = fmt else endDate = fmt
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
        tvStartDate.setOnClickListener { showDatePicker(tvStartDate, true) }
        tvEndDate.setOnClickListener   { showDatePicker(tvEndDate, false) }
        btnApplyFilter.setOnClickListener { loadCategoryLimit(categoryName) }

        // add txn button
        btnAddTxn.setOnClickListener {
            startActivity(Intent(this, add_trans::class.java))
            finish()
        }

        // initial load
        loadCategoryLimit(categoryName)

        // back arrow
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun loadCategoryLimit(categoryName: String) {
        val uid = auth.currentUser?.uid ?: return
        if (categoryName == "All Transactions") {
            // sum all category limits
            db.collection("users").document(uid)
                .collection("categories")
                .get()
                .addOnSuccessListener { snap ->
                    var totalLimit = 0L
                    snap.documents.forEach { totalLimit += it.getLong("amount") ?: 0L }
                    fetchTransactions(categoryName, totalLimit)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error loading overall limit: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            // single category limit
            db.collection("users").document(uid)
                .collection("categories")
                .whereEqualTo("name", categoryName)
                .limit(1)
                .get()
                .addOnSuccessListener { snap ->
                    val limit = snap.documents.firstOrNull()?.getLong("amount") ?: 0L
                    fetchTransactions(categoryName, limit)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error loading category limit: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun fetchTransactions(categoryName: String, limit: Long) {
        val uid = auth.currentUser?.uid ?: return
        val ref = db.collection("users").document(uid).collection("transactions")
        val query = if (categoryName == "All Transactions") ref
        else ref.whereEqualTo("category", categoryName)

        query.get()
            .addOnSuccessListener { docs ->
                val list = mutableListOf<Transaction>()
                var total = 0.0

                docs.forEach { doc ->
                    val name    = doc.getString("name") ?: ""
                    val amt     = doc.getDouble("amount") ?: 0.0
                    val details = doc.getString("details") ?: ""
                    val dateStr = doc.getString("date") ?: ""
                    val image   = doc.getString("image") ?: ""
                    val date    = try { sdf.parse(dateStr) } catch (_: Exception) { null }

                    // filter by date picker
                    val inRange = if (startDate != null && endDate != null && date != null) {
                        val start = sdf.parse(startDate!!)
                        val end   = sdf.parse(endDate!!)
                        date in start..end
                    } else true

                    if (inRange) {
                        total += amt
                        list.add(Transaction(name, amt, details, dateStr, image))
                    }
                }

                // bind totals with currency formatting
                tvTotalAmount.text = formatInSelectedCurrency(total)
                val remaining      = (limit.toDouble() - total).coerceAtLeast(0.0)
                tvRemaining.text   = formatInSelectedCurrency(remaining)

                // progress bar
                pbBalance.max = 100
                val pct = if (limit == 0L) 100
                else ((total / limit * 100).coerceIn(0.0, 100.0)).toInt()
                pbBalance.progress = pct

                // recycler
                rvTransactions.layoutManager = LinearLayoutManager(this)
                rvTransactions.adapter = TransactionAdapter(list) { txn ->
                    startActivity(Intent(this, TransactionDetailActivity::class.java).apply {
                        putExtra("title",   txn.name)
                        putExtra("amount",  txn.amount.toString())
                        putExtra("details", txn.details)
                        putExtra("date",    txn.date)
                        putExtra("image",   txn.image)
                    })
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching transactions: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    companion object {
        const val EXTRA_CATEGORY = "EXTRA_CATEGORY"
    }
}
