package com.st10194321.centsibletest

//import TransactionAdapter
import android.app.DatePickerDialog
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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


    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView
    private lateinit var btnApplyFilter: Button

    private var startDate: String? = null
    private var endDate: String? = null


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
        //spinnerMonth      = findViewById(R.id.spinnerMonthFilter)
        tvOverallLabel    = findViewById(R.id.tvOverallLabel)
        tvMonthLabel      = findViewById(R.id.tvMonthLabel)
        tvRemaining       = findViewById(R.id.tvRemaining)
        tvTotalAmount     = findViewById(R.id.tvTotalAmount)
        pbBalance         = findViewById(R.id.pbBalance)
        btnAddTxn         = findViewById(R.id.btnAddTxn)
        tvStartDate = findViewById(R.id.tvStartDate)
        tvEndDate = findViewById(R.id.tvEndDate)
        btnApplyFilter = findViewById(R.id.btnApplyFilter)



        // get category name from intent
        categoryName = intent.getStringExtra(EXTRA_CATEGORY) ?: run {
            Toast.makeText(this, "No category specified", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        loadCategoryLimit(categoryName)


        // set default labels
        tvOverallLabel.text = categoryName
        tvMonthLabel.text =
            DateFormatSymbols().months[Calendar.getInstance().get(Calendar.MONTH)]


//        // populate month filter spinner
//        val months = resources.getStringArray(R.array.month_filter_entries)
//        val monthAdapter = ArrayAdapter(
//            this,
//            R.layout.spinner_item,
//            months
//        ).also { it.setDropDownViewResource(R.layout.spinner_dropdown_item) }
//        spinnerMonth.adapter = monthAdapter

//Author: John Cowan
//Accessibiltiy: https://stackoverflow.com/questions/65556362/android-kotlin-get-value-of-selected-spinner-item
//Date Accessed: 24/04/2025

//        // spinner selection listener
//        spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>, view: View?, pos: Int, id: Long
//            ) {
//                selectedMonthIndex = pos
//
//                //update the month shown in the card
//                val monthNames = resources.getStringArray(R.array.month_filter_entries)
//                tvMonthLabel.text = if (pos == 0) "All" else monthNames[pos]
//
//                loadCategoryLimit(categoryName)
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {}
//        }

        // “add transaction” button
        btnAddTxn.setOnClickListener {
            startActivity(Intent(this, add_trans::class.java))
            finish()
        }
        fun showDatePicker(targetView: TextView, isStart: Boolean) {
            val calendar = Calendar.getInstance()
            val listener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                val formatted = String.format("%02d/%02d/%04d", day, month + 1, year)
                targetView.text = formatted
                if (isStart) startDate = formatted else endDate = formatted
            }

            DatePickerDialog(this, listener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        tvStartDate.setOnClickListener { showDatePicker(tvStartDate, true) }
        tvEndDate.setOnClickListener { showDatePicker(tvEndDate, false) }


        btnApplyFilter.setOnClickListener {
            loadCategoryLimit(categoryName)
        }


    }

    private fun loadCategoryLimit(categoryName: String) {
        if (categoryName == "All Transactions") {
            fetchTransactions(categoryName, 0L) // 0 means no limit
            return
        }

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
                Toast.makeText(this, "Error loading limit: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
    private fun fetchTransactions(categoryName: String, limit: Long) {
        val uid = auth.currentUser?.uid ?: return
        val transactionsRef = db.collection("users")
            .document(uid)
            .collection("transactions")

        val query = if (categoryName == "All Transactions") {
            transactionsRef
        } else {
            transactionsRef.whereEqualTo("category", categoryName)
        }

        query.get()
            .addOnSuccessListener { docs ->
                val list = mutableListOf<Transaction>()
                var total = 0.0

                for (doc in docs) {
                    val name = doc.getString("name") ?: ""
                    val amount = doc.getDouble("amount") ?: 0.0
                    val details = doc.getString("details") ?: ""
                    //val date = doc.getString("date") ?: ""
                    val date = doc.getString("date") ?: ""
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                    val itemDate = try {
                        sdf.parse(date)
                    } catch (e: Exception) {
                        null
                    }

                    val isInDateRange = if (startDate != null && endDate != null && itemDate != null) {
                        val start = sdf.parse(startDate!!)
                        val end = sdf.parse(endDate!!)
                        itemDate in start..end
                    } else true

                    val image = doc.getString("image") ?: ""

                    val month = date.split("/").getOrNull(1)?.toIntOrNull() ?: 0

                    if ((selectedMonthIndex == 0 || month == selectedMonthIndex) && isInDateRange) {
                        total += amount
                        list.add(Transaction(name, amount, details, date, image))
                    }

                }

                val remaining = (limit - total).coerceAtLeast(0.0)
                tvRemaining.text = "R%.2f".format(remaining)
                tvTotalAmount.text = "R%.2f".format(total)
                val pct = if (limit == 0L) 0 else ((total / limit * 100).coerceIn(0.0, 100.0)).toInt()
                pbBalance.progress = pct

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
                Toast.makeText(this, "Error fetching transactions: ${e.message}", Toast.LENGTH_LONG).show()
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

//    private fun loadTransactions(month: String) {
//        val index = resources.getStringArray(R.array.month_filter_entries).indexOf(month)
//        if (index != -1) {
//            selectedMonthIndex = index
//            spinnerMonth.setSelection(index)
//            loadCategoryLimit(categoryName)
//        }
//    }


    companion object {
        const val EXTRA_CATEGORY = "EXTRA_CATEGORY"
    }
}
