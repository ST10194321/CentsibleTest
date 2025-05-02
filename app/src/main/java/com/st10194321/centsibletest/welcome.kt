package com.st10194321.centsibletest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class welcome : AppCompatActivity() {

private lateinit var btnLog:Button
private lateinit var btnReg:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnLog = findViewById(R.id.btnLog)
        btnReg = findViewById(R.id.btnReg)

        //Leads to login page
        btnLog.setOnClickListener {
                 val i = Intent(this, signin::class.java)
               startActivity(i)
             }

        //Leads to signup page
        btnReg.setOnClickListener {
            val i = Intent(this, signupD::class.java)
            startActivity(i)
        }
        //Author: Android Developers
        //Accessibiltiy: https://developer.android.com/reference/android/view/View#setOnClickListener(android.view.View.OnClickListener)
        //Date Accessed: 15/04/2025
    }
}