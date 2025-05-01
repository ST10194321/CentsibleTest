package com.st10194321.centsibletest

import android.os.Bundle
import android.widget.TextView
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class TransactionDetailActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_detail)

        val title = intent.getStringExtra("title")
        val amount = intent.getStringExtra("amount")
        val details = intent.getStringExtra("details")
        val date = intent.getStringExtra("date")
        val image = intent.getStringExtra("image")
        val imageView = findViewById<ImageView>(R.id.imageTransaction)

        if (!image.isNullOrEmpty()) {
            try {
                val decodedBytes = android.util.Base64.decode(image, android.util.Base64.DEFAULT)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val detailsTextView = findViewById<TextView>(R.id.textTransactionDetails)
        detailsTextView.text = "Title: $title\nAmount: $amount\nDetails: $details\n" +
                "Date: $date"

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
    }

}