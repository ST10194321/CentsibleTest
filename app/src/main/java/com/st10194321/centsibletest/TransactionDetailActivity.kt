package com.st10194321.centsibletest

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

//Author: Liam Brown
//Accessibility: https://stackoverflow.com/questions/65346398/how-to-post-an-image-as-a-post-kotlin-firestore
//Date accessed: 29/04/2025

class TransactionDetailActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_detail)
        enableEdgeToEdge()


        val title = intent.getStringExtra("title")
        val amount = intent.getStringExtra("amount")
        val details = intent.getStringExtra("details")
        val date = intent.getStringExtra("date")
        val image = intent.getStringExtra("image")
        val imageView = findViewById<ImageView>(R.id.imageTransaction)
        val cardImage = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardImage)


        if (!image.isNullOrEmpty()) {
            try {
                val decodedBytes = android.util.Base64.decode(image, android.util.Base64.DEFAULT)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                imageView.setImageBitmap(bitmap)
                imageView.visibility = View.VISIBLE
                cardImage.visibility      = View.VISIBLE


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