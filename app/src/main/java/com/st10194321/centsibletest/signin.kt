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
import com.st10194321.centsibletest.databinding.ActivitySigninBinding

import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt


class signin : AppCompatActivity() {

    private lateinit var binding: ActivitySigninBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signin)

        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)




        binding.btnBack.setOnClickListener {
             val i = Intent(this, welcome::class.java)
        startActivity(i)
       }


        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

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