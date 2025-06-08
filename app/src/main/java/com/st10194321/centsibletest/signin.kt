package com.st10194321.centsibletest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.st10194321.centsibletest.databinding.ActivitySigninBinding
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt
import java.util.concurrent.Executor

class signin : AppCompatActivity() {

    private lateinit var binding: ActivitySigninBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, welcome::class.java))
        }

        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, signup::class.java))
        }

        // Email/password sign-in
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                    } else {
                        lifecycleScope.launch {
                            val localUser = AppDatabase.getDatabase(this@signin).userDao().getUserByEmail(email)
                            if (localUser != null && BCrypt.checkpw(password, localUser.password)) {
                                Toast.makeText(this@signin, "Offline login successful", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@signin, MainActivity::class.java))
                            } else {
                                Toast.makeText(this@signin, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
        }

        setupBiometricLogin()
    }

    private fun setupBiometricLogin() {
        // Setup Biometric Authentication
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(applicationContext, "Authentication succeeded!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@signin, MainActivity::class.java))
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(applicationContext, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Use your fingerprint to login")
            .setNegativeButtonText("Cancel")
            .build()

        binding.btnBiometric.setOnClickListener {
            val biometricManager = BiometricManager.from(this)
            when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    biometricPrompt.authenticate(promptInfo)
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    Toast.makeText(this, "Biometric hardware not available", Toast.LENGTH_SHORT).show()
                }
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    Toast.makeText(this, "Biometric hardware currently unavailable", Toast.LENGTH_SHORT).show()
                }
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    Toast.makeText(this, "No biometric credentials enrolled", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "Biometric Login not supported", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
}

//Author: Firebase Documentation Team
//Accessibiltiy: https://firebase.google.com/docs/firestore/query-data/get-data?platform=android
//Date Accessed: 10/04/2025

//Author: Damien Miller (jBCrypt)
//Accessibiltiy: https://www.mindrot.org/projects/jBCrypt/
//Date Accessed: 10/04/2025


//Author: Firebase Documentation Team
//Accessibiltiy: https://firebase.google.com/docs/auth/android/manage-users#sign_in
//Date Accessed: 10/04/2025

//Author: Thornsby, J
//Accessibility: https://www.androidauthority.com/add-fingerprint-authentication-app-biometricprompt-943784/
//Date Accessed: 29/05/2025