package com.st10194321.centsibletest

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.st10194321.centsibletest.databinding.ActivityAddBugCatBinding


class addBugCat : AppCompatActivity() {

    private lateinit var binding: ActivityAddBugCatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_bug_cat)

        binding = ActivityAddBugCatBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val etOccurrence = findViewById<TextInputEditText>(R.id.etOccurrence)
        etOccurrence.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->

                etOccurrence.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
            }, year, month, day)

            datePickerDialog.show()
        }
        }
    }
