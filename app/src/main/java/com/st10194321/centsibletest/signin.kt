package com.st10194321.centsibletest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.st10194321.centsibletest.databinding.ActivitySigninBinding

import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt


class signin : AppCompatActivity() {
    // view binding
    private lateinit var binding: ActivitySigninBinding
    // Firebase auth
    private lateinit var auth: FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signin)

        // init auth
        auth = FirebaseAuth.getInstance()

        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)



        //back button goes to previous screen
        binding.btnBack.setOnClickListener {
             val i = Intent(this, welcome::class.java)
        startActivity(i)
       }

        //sign up button goes to sign up screen
        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, signup::class.java))
        }

        // sign‑in logic
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
                Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            //checks user with registry credentials stored in firesbase auth
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                    } else {
                        //roomdb login
                        lifecycleScope.launch {
                            val localUser = AppDatabase
                                .getDatabase(this@signin)
                                .userDao()
                                .getUserByEmail(email)
                            if (localUser != null && BCrypt.checkpw(password, localUser.password)) {
                                Toast.makeText(this@signin, "Offline login successful", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@signin, MainActivity::class.java))
                            } else {
                                Toast.makeText(
                                    this@signin,
                                    "Login failed: ${task.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            //Author: Firebase Documentation Team
                        // Accessibiltiy: https://firebase.google.com/docs/firestore/query-data/get-data?platform=android
                        // Date Accessed: 10/04/2025

                            //Author: Damien Miller (jBCrypt)
                        // Accessibiltiy: https://www.mindrot.org/projects/jBCrypt/
                        // Date Accessed: 10/04/2025

                        }
                    }
                }

        }

        }
    }
//Author: Firebase Documentation Team
//Accessibiltiy: https://firebase.google.com/docs/auth/android/manage-users#sign_in
//Date Accessed: 10/04/2025