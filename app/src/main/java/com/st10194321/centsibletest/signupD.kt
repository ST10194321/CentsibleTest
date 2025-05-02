package com.st10194321.centsibletest

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.st10194321.centsibletest.databinding.ActivitySignupBinding
import com.st10194321.centsibletest.databinding.ActivitySignupDBinding
import java.sql.Date
import java.util.Locale



class signupD : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivitySignupDBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignupDBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }




        //leads back to the welcome page
        binding.btnBackUp.setOnClickListener {
            val i = Intent(this, welcome::class.java)
            startActivity(i)
            finish()
        }

        // date‑of‑birth picker
        binding.etDOB.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    binding.etDOB.setText(String.format("%02d/%02d/%04d", day, month + 1, year))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

//Author: Chetan R
//Accessibiltiy: https://stackoverflow.com/questions/14933330/datepicker-how-to-popup-datepicker-when-entering-edittext
//Date Accessed: 21/04/2025

        //pass personal details data to signup
        binding.btnContinue.setOnClickListener {
            val firstName = binding.etUp.text.toString().trim()
            val lastName  = binding.etPasswordUp.text.toString().trim()
            val phone     = binding.etPhoneUp.text.toString().trim()
            val dob       = binding.etDOB.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty() ||
                phone.isEmpty()     || dob.isEmpty()
            ) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Intent(this, signup::class.java).apply {
                putExtra("FIRST_NAME", firstName)
                putExtra("LAST_NAME",  lastName)
                putExtra("PHONE",      phone)
                putExtra("DOB",        dob)
            }.also { startActivity(it) }
        }
    }



    }
