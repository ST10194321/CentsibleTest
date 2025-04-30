package com.st10194321.centsibletest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class editprofile : AppCompatActivity() {

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var saveButton: Button

    val instance = signup()
    val fName = instance.firstName
    val lName = instance.lastName
    val phone = instance.phone

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editprofile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        etFirstName = findViewById(R.id.editFirstName)
        etLastName = findViewById(R.id.editLastName)
        etPhoneNumber = findViewById(R.id.editPhoneNumber)
        saveButton = findViewById(R.id.saveButton)

        etFirstName.setText(fName)
        etLastName.setText(lName)
        etPhoneNumber.setText(phone)

        saveButton.setOnClickListener {

            val newFirstName = etFirstName.text.toString()
            val newLastName = etLastName.text.toString()
            val newPhoneNumber = etPhoneNumber.text.toString()

            val auth: FirebaseAuth = FirebaseAuth.getInstance()
            val db: FirebaseFirestore = FirebaseFirestore.getInstance()


            val uid = auth.currentUser!!.uid

            val profile = mapOf(
                "firstName" to newFirstName,
                "lastName" to newLastName,
                "phone" to newPhoneNumber
            )
            db.collection("users")
                .document(uid)
                .set(profile, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Profile save failed:\n${e.message}", Toast.LENGTH_LONG)
                        .show()
                }


        }
    }
}