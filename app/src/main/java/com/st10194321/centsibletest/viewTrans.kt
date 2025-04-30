package com.st10194321.centsibletest

import TransactionAdapter
import android.content.Intent
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
import com.st10194321.centsibletest.viewBugCat.Companion.EXTRA_CATEGORY

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

//    private fun fetchTransactions() {
//        val user = auth.currentUser
//        if (user == null) {
//            Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        db.collection("users")
//            .document(user.uid)
//            .collection("transactions")
//            .get()
//            .addOnSuccessListener { documents ->
//                val transactions = mutableListOf<Transaction>()
//                for (document in documents) {
//                    val name = document.getString("name") ?: ""
//                    val amount = document.getDouble("amount") ?: 0.0
//                    val date = document.getString("date") ?: ""
//                    transactions.add(Transaction(name, amount, date))
//                }
//
//                // Set up the RecyclerView adapter with the fetched transactions
//                adapter = TransactionAdapter(transactions) { transaction ->
//                    // Handle item click (optional)
//                    Toast.makeText(this, "Clicked: ${transaction.name}", Toast.LENGTH_SHORT).show()
//                }
//                rvTransactions.layoutManager = LinearLayoutManager(this)
//                rvTransactions.adapter = adapter
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(this, "Error fetching transactions: ${e.message}", Toast.LENGTH_LONG).show()
//            }
//    }
private fun fetchTransactions() {
    val user = auth.currentUser
    if (user == null) {
        Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show()
        return
    }

    val selectedCategory = intent.getStringExtra(viewBugCat.EXTRA_CATEGORY)

    db.collection("users")
        .document(user.uid)
        .collection("transactions")
        .whereEqualTo("category", selectedCategory) // 🔍 Filter by selected category
        .get()
        .addOnSuccessListener { documents ->
            val transactions = mutableListOf<Transaction>()
            for (document in documents) {
                val name = document.getString("name") ?: ""
                val amount = document.getDouble("amount") ?: 0.0
                val details = document.getString("details") ?: ""
                val date = document.getString("date") ?: ""
                transactions.add(Transaction(name, amount, details,date))
            }

            adapter = TransactionAdapter(transactions) { transaction ->
                Toast.makeText(this, "Clicked: ${transaction.name}", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, TransactionDetailActivity::class.java).apply {
                    //putExtra(EXTRA_CATEGORY, cat.name)
                    putExtra("title", transaction.name)
                    putExtra("amount", transaction.amount.toString())
                    putExtra("details", transaction.details)
                    putExtra("date", transaction.date)
                }
                startActivity(intent)
            }
            rvTransactions.layoutManager = LinearLayoutManager(this)
            rvTransactions.adapter = adapter
        }
        .addOnFailureListener { e ->
            Toast.makeText(this, "Error fetching transactions: ${e.message}", Toast.LENGTH_LONG).show()
        }
}

}

