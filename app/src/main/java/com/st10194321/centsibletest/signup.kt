package com.st10194321.centsibletest

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.st10194321.centsibletest.databinding.ActivitySignupBinding

class signup : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


    val firstName by lazy { intent.getStringExtra("FIRST_NAME") ?: "" }
    val lastName  by lazy { intent.getStringExtra("LAST_NAME")  ?: "" }
    val phone     by lazy { intent.getStringExtra("PHONE")      ?: "" }
    val dob       by lazy { intent.getStringExtra("DOB")        ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        binding.btnSignUp.setOnClickListener {
            val email    = binding.etEmailUp.text.toString().trim()
            val password = binding.etPasswordUp.text.toString().trim()
            val conPass  = binding.etConPasswordUp.text.toString().trim()

            if (email.isBlank() || password.isBlank() || conPass.isBlank()) {
                Toast.makeText(this, "Email and password required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != conPass) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser!!.uid

                        val profile = mapOf(
                            "firstName" to firstName,
                            "lastName"  to lastName,
                            "phone"     to phone,
                            "dob"       to dob,
                            "email"     to email
                        )

                        db.collection("users")
                            .document(uid)
                            .set(profile)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Profile save failed:\n${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(
                            this,
                            "Registration failed:\n${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}
