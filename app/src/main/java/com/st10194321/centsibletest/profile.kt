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
import com.st10194321.centsibletest.databinding.ActivityProfileBinding // Corrected import
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class profile : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var capturedBitmap: Bitmap? = null
    private lateinit var cameraLauncher: ActivityResultLauncher<Void?>
    private lateinit var spinnerCurrency: Spinner
    // val achivements : achievements = achievements() // This line is not used and can be removed

    // Firebase
    private val auth = FirebaseAuth.getInstance()
    // Using safe call for auth.currentUser to avoid NullPointerException if user is not logged in
    private val db   = FirebaseFirestore.getInstance()
    private val uid  = auth.currentUser?.uid ?: "" // Handle null UID by providing an empty string

    // SharedPreferences for “selected_currency”
    private val PREFS_NAME = "centsible_prefs"
    private val KEY_SEL_CURRENCY = "selected_currency"
    private lateinit var prefs: SharedPreferences

    // Hard-code the currencies you want to support:
    private val availableCurrencies = listOf("ZAR", "USD", "EUR", "GBP")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make sure CurrencyRepository has been initialized in Application.onCreate()
        CurrencyRepository.initialize(applicationContext)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        // Important: Check if UID is valid before attempting Firestore operations
        if (uid.isEmpty()) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, signin::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return // Stop further execution
        }

        // --- Fetch Achievements (existing code) ---
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
                        // Add more cases for other achievements if needed
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load achievements: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        // --- End Fetch Achievements ---


        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // ====== Spinner Currency Logic ======
        spinnerCurrency = findViewById(R.id.spinnerCurrency)

        val adapter = ArrayAdapter(
            this,
            R.layout.spinner_item,
            availableCurrencies
        ).apply {
            setDropDownViewResource(R.layout.spinner_item)
        }

        spinnerCurrency.adapter = adapter

        spinnerCurrency.setSelection(
            availableCurrencies.indexOf(prefs.getString(KEY_SEL_CURRENCY, "ZAR"))
                .coerceAtLeast(0)
        )

        spinnerCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                (view as? TextView)?.setTextColor(Color.WHITE) // Set text color for selected item

                val chosen = availableCurrencies[position]
                prefs.edit().putString(KEY_SEL_CURRENCY, chosen).apply()
                CoroutineScope(Dispatchers.IO).launch {
                    CurrencyRepository.get().fetchLatestRates()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) = Unit
        }
        // ====== End Spinner Logic ======

        // --- Fetch User Profile Data (Image and Name) ---
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Load Profile Image
                    val base64Image = document.getString("image")
                    if (!base64Image.isNullOrEmpty()) {
                        val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        binding.profileImage.setImageBitmap(bitmap)
                    }

                    // *** FETCH AND DISPLAY USER NAME HERE ***
                    val firstName = document.getString("firstName")
                    val lastName = document.getString("lastName")

                    val fullName = if (!firstName.isNullOrEmpty() && !lastName.isNullOrEmpty()) {
                        "$firstName $lastName"
                    } else if (!firstName.isNullOrEmpty()) {
                        firstName
                    } else if (!lastName.isNullOrEmpty()) {
                        lastName
                    } else {
                        "Guest User" // Default or fallback name if no name parts are found
                    }
                    binding.userName.text = fullName // Set the name to your TextView with ID 'userName'
                    // *****************************************

                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load profile data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        // --- End Fetch User Profile Data ---


        // ====== Navigation bar (Keeping the first set of listeners, assuming they are the correct ones) ======
        binding.iconHome1.setOnClickListener { // These IDs refer to the LinearLayout wrappers
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        binding.iconCategories1.setOnClickListener {
            startActivity(Intent(this, viewBugCat::class.java))
            finish()
        }
        binding.iconProfile1.setOnClickListener {
            // Already here, no need to restart self. If a refresh is truly needed, handle it carefully.
            // For example, you could call 'recreate()' but that restarts the whole activity lifecycle.
            // Simply doing nothing or showing a toast might be better here.
            // Toast.makeText(this, "You are already on the Profile screen.", Toast.LENGTH_SHORT).show()
        }
        // ====== End Navigation bar ======


        // --- Other Clicks ---
        binding.tvEditProfile.setOnClickListener {
            startActivity(Intent(this, editprofile::class.java))
        }

        binding.cardSetIncome.setOnClickListener {
            startActivity(Intent(this, SetIncomeActivity::class.java))
        }

        binding.cardFinancialHealth.setOnClickListener {
            startActivity(Intent(this, HealthActivity::class.java))
        }

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
                // You might reconsider this 'refresh' logic as it restarts the activity.
                // It's generally better to update the UI directly if possible.
                startActivity(Intent(this, profile::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}