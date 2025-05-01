package com.st10194321.centsibletest

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.st10194321.centsibletest.databinding.ActivityAddTransBinding
import java.io.ByteArrayOutputStream
import java.util.*

class add_trans : AppCompatActivity() {

    private lateinit var binding: ActivityAddTransBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db   by lazy { FirebaseFirestore.getInstance() }

    private lateinit var categoryAdapter: ArrayAdapter<String>
    private val categories = mutableListOf<String>()

    private lateinit var captureImageButton: ImageButton
    private lateinit var imageView: ImageView

    private var capturedBitmap: Bitmap? = null
    private lateinit var cameraLauncher: ActivityResultLauncher<Void?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddTransBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        //Author: John Cowan
        //Accessibiltiy: https://stackoverflow.com/questions/65556362/android-kotlin-get-value-of-selected-spinner-item
        //Date Accessed: 24/04/2025
        // Spinner setup for categories
        categoryAdapter = ArrayAdapter(this,
            R.layout.spinner_item, categories).also {
            it.setDropDownViewResource(R.layout.spinner_dropdown_item)
            binding.spinnerCategory.adapter = it
        }

        // Image capture ->> allows users to upload picture in every transaction
        captureImageButton = findViewById(R.id.btnCaptureImage)
        imageView = findViewById(R.id.btnCaptureImage)
        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->
            if (bitmap != null) {
                capturedBitmap = bitmap
                imageView.setImageBitmap(bitmap)
            }
        }
        captureImageButton.setOnClickListener {
            cameraLauncher.launch(null)
        }

        // Load categories ->> allows users to pick from existing categories
        auth.currentUser?.uid?.let { uid ->
            db.collection("users").document(uid)
                .collection("categories")
                .get()
                .addOnSuccessListener { snap ->
                    categories.clear()
                    snap.documents.mapNotNull { it.getString("name") }
                        .also { categories.addAll(it) }
                    categoryAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load categories", Toast.LENGTH_SHORT).show()
                }
        }

        // Date picker
        binding.etTxnDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    binding.etTxnDate.setText("%02d/%02d/%04d".format(d, m + 1, y))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Add transaction and saves transactions to database
        binding.btnAddToCategory.setOnClickListener {
            val user = auth.currentUser
            if (user == null) {
                Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val name     = binding.etTxnName.text.toString().trim()
            val amountStr= binding.etTxnAmount.text.toString().trim()
            val details  = binding.etTxnDetails.text.toString().trim()
            val category = binding.spinnerCategory.selectedItem.toString()
            val date     = binding.etTxnDate.text.toString().trim()

            // Validate required fields
            if (name.isEmpty() || amountStr.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please complete all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0.0) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Build transaction
            val txn = mutableMapOf<String, Any>(
                "name"      to name,
                "amount"    to amount,
                "details"   to details,
                "category"  to category,
                "date"      to date,
                "timestamp" to System.currentTimeMillis()
            )


            convertImageToBase64()?.let { base64 ->
                txn["image"] = base64
            }


            db.collection("users").document(user.uid)
                .collection("transactions")
                .add(txn)
                .addOnSuccessListener {
                    Toast.makeText(this, "Transaction added!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
    }

//    Author: Mughira Dar
//    Accessibilty: https://stackoverflow.com/questions/58955434/how-to-convert-base64-string-into-image-in-kotlin-android
//    Date: 28/04/2025

    // Returns a Base64 string or null if no image was captured
    private fun convertImageToBase64(): String? {
        val bmp = capturedBitmap ?: return null
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 50, stream)
        val bytes = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }
}
