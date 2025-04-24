package com.st10194321.centsibletest

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.st10194321.centsibletest.databinding.ActivitySignupBinding
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt

class signup : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnBack.setOnClickListener {
            val i = Intent(this, welcome::class.java)
            startActivity(i)
            finish()
        }

        auth = FirebaseAuth.getInstance()


        binding.btnSignUp.setOnClickListener {
            val email = binding.etEmailUp.text.toString().trim()
            val password = binding.etPasswordUp.text.toString().trim()
            val conPass = binding.etConPasswordUp.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()||conPass.isEmpty()) {
                Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(password != conPass){
                Toast.makeText(this,"Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this){
                    task ->

                if(task.isSuccessful){
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                    val i = Intent(this,MainActivity::class.java)
                    startActivity(i)
                    finish()

                }else{
                    Toast.makeText(this,"Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    val i = Intent(this,signup::class.java)
                    startActivity(i)
                    finish()
                }
            }




            /* // Hash  password using BCrypt
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
             }*/
        }
    }
}
