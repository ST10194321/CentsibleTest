package com.st10194321.centsibletest

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.st10194321.centsibletest.databinding.ActivityAddBugCatBinding
import java.util.Calendar

class addBugCat : AppCompatActivity() {

    private lateinit var binding: ActivityAddBugCatBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        //binding for xml elements
        binding = ActivityAddBugCatBinding.inflate(layoutInflater)
        setContentView(binding.root)


        ViewCompat.setOnApplyWindowInsetsListener(binding.homeLayout) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }


        //seekbar for user to input amount
        binding.seekBarAmount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, prog: Int, fromUser: Boolean) {

                if (!binding.etAmountValue.isFocused) {
                    binding.etAmountValue.setText(prog.toString())
                }
            }

            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })

        //an editable text for user to input specific amount
        binding.etAmountValue.addTextChangedListener { editable ->
            val str = editable.toString()
            val num = str.toIntOrNull()
            if (num != null) {
                val value = num.coerceIn(0, binding.seekBarAmount.max)
                if (binding.seekBarAmount.progress != value) {
                    binding.seekBarAmount.progress = value
                }
            }
        }
//Author: Kotlin Documentation Team
//Accessibiltiy: https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.ranges/coerce-in.html
//Date Accessed: 21/04/2025

        //allows user to choose a date
        binding.etOccurrence.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    binding.etOccurrence.setText("%02d/%02d/%04d".format(d, m + 1, y))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
//Author: Chetan R
//Accessibiltiy: https://stackoverflow.com/questions/14933330/datepicker-how-to-popup-datepicker-when-entering-edittext
//Date Accessed: 21/04/2025

        //adds category to user
        binding.btnAddCategory.setOnClickListener {
            val user = auth.currentUser
            if (user == null) {
                Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            //assigning fields to details
            val name = binding.etCategoryName.text.toString().trim()
            val details = binding.etCategoryDetails.text.toString().trim()
            val occ = binding.etOccurrence.text.toString().trim()
            val amtText = binding.etAmountValue.text.toString().removePrefix("R")
            val amount = amtText.toLongOrNull() ?: 0L
            val type = if (binding.toggleButtonGroup.checkedButtonId == R.id.btnSaving)
                "Saving" else "Expense"


            //prompts user to enter name if empty
            if (name.isEmpty()) {
                Toast.makeText(this, "Enter a category name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            //fields for firestore
            val category = Category(
                name = name,
                type = type,
                details = details,
                amount = amount,
                occurrence = occ
            )


            //saves category in firestore in user collection
            db.collection("users")
                .document(user.uid)
                .collection("categories")
                .add(category)
                .addOnSuccessListener {
                    Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show()
                    val i = Intent(this, MainActivity::class.java)
                    startActivity(i)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
    }
}