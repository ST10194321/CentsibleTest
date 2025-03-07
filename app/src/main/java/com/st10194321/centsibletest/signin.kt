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
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt


class signin : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnsignin: Button
    private lateinit var btnGoogleSignIn: Button

    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signin)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnsignin = findViewById(R.id.btnSignIn)
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
             val i = Intent(this, welcome::class.java)
        startActivity(i)
       }

        btnsignin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
                Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val db = AppDatabase.getDatabase(this)
            lifecycleScope.launch {
                val user = db.userDao().getUserByEmail(email)
                if (user != null && BCrypt.checkpw(password, user.password)) {
                    Toast.makeText(this@signin, "Signed In Successfully", Toast.LENGTH_SHORT).show()
                    // Navigate to the main activity after successful sign-in
                    val intent = Intent(this@signin, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@signin, "Sign In Failed. Invalid credentials.", Toast.LENGTH_LONG).show()
                }
            }






        }
    }
}