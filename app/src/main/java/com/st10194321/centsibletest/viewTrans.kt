package com.st10194321.centsibletest

//import android.os.Bundle
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//
//class viewTrans : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_view_trans)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//    }
//}
import TransactionAdapter
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.st10194321.centsibletest.databinding.ActivityTransactionBinding
import com.st10194321.centsibletest.databinding.ActivityViewBugCatBinding
import com.st10194321.centsibletest.R
import com.st10194321.centsibletest.Transaction


//class viewTrans : AppCompatActivity() {
//
//    private lateinit var rvTransactions: RecyclerView
//    private lateinit var adapter: TransactionAdapter
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_view_trans)
//
//        rvTransactions = findViewById(R.id.rvTransactions)
//
//        // Example data
//        val transactions = listOf(
//            Transaction("Groceries", 150.75, "2025-04-25"),
//            Transaction("Fuel", 500.0, "2025-04-26"),
//            Transaction("Medical", 300.25, "2025-04-27"),
//            Transaction("Savings", 1000.0, "2025-04-28")
//        )
//
//        adapter = TransactionAdapter(transactions) { transaction ->
//            // Handle item click
//            // For now, just log or toast
//            Toast.makeText(this, "Clicked: ${transaction.name}", Toast.LENGTH_SHORT).show()
//        }
//
//        rvTransactions.layoutManager = LinearLayoutManager(this)
//        rvTransactions.adapter = adapter
//    }
//}

class viewTrans : AppCompatActivity() {

    private lateinit var rvTransactions: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_trans)

        rvTransactions = findViewById(R.id.rvTransactions)

        // Fetch transactions from Firestore
        fetchTransactions()
    }

    private fun fetchTransactions() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users")
            .document(user.uid)
            .collection("transactions")
            .get()
            .addOnSuccessListener { documents ->
                val transactions = mutableListOf<Transaction>()
                for (document in documents) {
                    val name = document.getString("name") ?: ""
                    val amount = document.getDouble("amount") ?: 0.0
                    val date = document.getString("date") ?: ""
                    transactions.add(Transaction(name, amount, date))
                }

                // Set up the RecyclerView adapter with the fetched transactions
                adapter = TransactionAdapter(transactions) { transaction ->
                    // Handle item click (optional)
                    Toast.makeText(this, "Clicked: ${transaction.name}", Toast.LENGTH_SHORT).show()
                }
                rvTransactions.layoutManager = LinearLayoutManager(this)
                rvTransactions.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching transactions: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}

