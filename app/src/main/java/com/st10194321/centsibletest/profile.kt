package com.st10194321.centsibletest

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.st10194321.centsibletest.databinding.ActivityProfileBinding
import com.st10194321.centsibletest.CurrencyRepository
import com.st10194321.centsibletest.SetIncomeActivity
import com.st10194321.centsibletest.HealthActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class profile : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var capturedBitmap: Bitmap? = null
    private lateinit var cameraLauncher: ActivityResultLauncher<Void?>
    private lateinit var spinnerCurrency: Spinner
    val achivements : achievements = achievements()

    // Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()
    private val uid  = auth.currentUser!!.uid

    // SharedPreferences for “selected_currency”
    private val PREFS_NAME = "centsible_prefs"
    private val KEY_SEL_CURRENCY = "selected_currency"
    private lateinit var prefs: SharedPreferences

    // Hard‐code the currencies you want to support:
    private val availableCurrencies = listOf("ZAR", "USD", "EUR", "GBP")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make sure CurrencyRepository has been initialized in Application.onCreate()
        // (or you can call it here if you never did it elsewhere):
        CurrencyRepository.initialize(applicationContext)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        if (uid != null) {
            db.collection("users").document(uid)
                .collection("achievements")
                .get()
                .addOnSuccessListener { snapshot ->
                    for (doc in snapshot.documents) {
                        val name = doc.getString("name") ?: continue

                        when (name) {
                            "First Steps" -> {
                                findViewById<ImageView>(R.id.ivFirstStepsProfile).visibility = View.VISIBLE
                            }
                            "Budget Beginner" -> {
                                findViewById<ImageView>(R.id.ivBudgetBeginnerProfile).visibility = View.VISIBLE
                            }
                            "Smart Spender" -> {
                                findViewById<ImageView>(R.id.ivSmartSpenderProfile).visibility = View.VISIBLE
                            }
                        }
                    }
                }
        }

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // ====== BIND THE NEW SPINNER ======
        spinnerCurrency = findViewById(R.id.spinnerCurrency)

// use YOUR spinner_item.xml which already has white text
        val adapter = ArrayAdapter(
            this,
            R.layout.spinner_item,          // ← your custom layout
            availableCurrencies
        ).apply {
            // if you want the dropdown rows white too, point them here
            setDropDownViewResource(R.layout.spinner_item)
        }

        spinnerCurrency.adapter = adapter

// … the rest of your selection logic stays the same …
        spinnerCurrency.setSelection(
            availableCurrencies.indexOf(prefs.getString(KEY_SEL_CURRENCY, "ZAR"))
                .coerceAtLeast(0)
        )

        spinnerCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                // if you want to recolor the closed‐spinner text (just in case)
                (view as? TextView)?.setTextColor(Color.WHITE)

                val chosen = availableCurrencies[position]
                prefs.edit().putString(KEY_SEL_CURRENCY, chosen).apply()
                CoroutineScope(Dispatchers.IO).launch {
                    CurrencyRepository.get().fetchLatestRates()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) = Unit
        }

        // ====== END SPINNER LOGIC ======

        // Load profile image from Firestore if available
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val base64Image = document.getString("image")
                    if (!base64Image.isNullOrEmpty()) {
                        val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        binding.profileImage.setImageBitmap(bitmap)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load profile image: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        // Navigation bar
        binding.iconHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        binding.iconCategories.setOnClickListener {
            startActivity(Intent(this, viewBugCat::class.java))
            finish()
        }
        binding.iconProfile.setOnClickListener {
            // Already here, but if you want to refresh:
            startActivity(Intent(this, profile::class.java))
            finish()
        }
        binding.iconHome1.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        binding.iconCategories1.setOnClickListener {
            startActivity(Intent(this, viewBugCat::class.java))
            finish()
        }
        binding.iconProfile1.setOnClickListener {
            // Already here, but if you want to refresh:
            startActivity(Intent(this, profile::class.java))
            finish()
        }

        // Edit profile screen
        binding.tvEditProfile.setOnClickListener {
            startActivity(Intent(this, editprofile::class.java))
        }

        binding.cardSetIncome.setOnClickListener {
            startActivity(Intent(this, SetIncomeActivity::class.java))
        }

        binding.cardFinancialHealth.setOnClickListener {
            startActivity(Intent(this, HealthActivity::class.java))
        }


        // View goals screen
        binding.cardViewGoals.setOnClickListener {
            startActivity(Intent(this, viewgoals::class.java))
        }
        binding.achievementsLabel.setOnClickListener{
            val i = Intent(this, achievements::class.java)
            startActivity(i)
            finish()
        }

        // Logout
        binding.cardLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, signin::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Set up camera to take profile picture
        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->
            if (bitmap != null) {
                capturedBitmap = bitmap
                binding.profileImage.setImageBitmap(bitmap)
                uploadProfileImage()
            }
        }

        // Launch camera on profile image click
        binding.profileImage.setOnClickListener {
            cameraLauncher.launch(null)
        }
    }

    /** Convert capturedBitmap → Base64, then save under “image” field in Firestore. */
    private fun uploadProfileImage() {
        val bmp = capturedBitmap ?: return
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 50, stream)
        val bytes = stream.toByteArray()
        val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)
        val txn = mapOf("image" to base64)
        db.collection("users").document(uid)
            .set(txn, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Profile Image added", Toast.LENGTH_SHORT).show()
                // Refresh this Activity to see changes
                startActivity(Intent(this, profile::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
