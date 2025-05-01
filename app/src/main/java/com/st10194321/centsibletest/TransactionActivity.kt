package com.st10194321.centsibletest

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.st10194321.centsibletest.databinding.ActivityTransactionBinding

class TransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAddTransaction.setOnClickListener {
            val user = auth.currentUser
            if (user == null) {
                Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val name = binding.etName.text.toString().trim()
            val amountText = binding.etAmount.text.toString().trim()
            val amount = amountText.toDoubleOrNull()

            //ensures name input is found
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a transaction name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //ensures amount input is found
            if (amount == null || amount <= 0.0) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val transaction = hashMapOf(
                "name" to name,
                "amount" to amount,
                "timestamp" to System.currentTimeMillis()
            )

            //saves to firestore
            db.collection("users")
                .document(user.uid)
                .collection("transactions")
                .add(transaction)
                .addOnSuccessListener {
                    Toast.makeText(this, "Transaction added", Toast.LENGTH_SHORT).show()
                    finish() // or redirect to main screen
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
