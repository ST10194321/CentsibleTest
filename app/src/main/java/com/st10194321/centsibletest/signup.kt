package com.st10194321.centsibletest

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.st10194321.centsibletest.databinding.ActivitySignupBinding
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt

class signup : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Initialize view binding
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply window insets using the root view from binding
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Back button navigates to welcome screen
        binding.btnBack.setOnClickListener {
            val i = Intent(this, welcome::class.java)
            startActivity(i)
            finish()
        }

        // Sign-up button logic
        binding.btnSignUp.setOnClickListener {
            val email = binding.etEmailUp.text.toString().trim()
            val password = binding.etPasswordUp.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Hash  password using BCrypt
            val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())

            // Access  Room database and insert the new user
            val db = AppDatabase.getDatabase(this)
            lifecycleScope.launch {
                val newUser = users(email = email, password = hashedPassword)
                db.userDao().insertUser(newUser)
                Toast.makeText(this@signup, "Sign Up Successful", Toast.LENGTH_SHORT).show()

                // Navigate to the sign in after successful sign-up
                val i = Intent(this@signup, signin::class.java)
                startActivity(i)
                finish()
            }
        }
    }
}
