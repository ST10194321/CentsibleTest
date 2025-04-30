package com.st10194321.centsibletest

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TransactionDetailActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_detail)

        val title = intent.getStringExtra("title")
        val amount = intent.getStringExtra("amount")
        val details = intent.getStringExtra("details")
        val date = intent.getStringExtra("date")

        val detailsTextView = findViewById<TextView>(R.id.textTransactionDetails)
        detailsTextView.text = "Title: $title\nAmount: $amount\nDetails: $details\n" +
                "Date: $date"

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
    }

}