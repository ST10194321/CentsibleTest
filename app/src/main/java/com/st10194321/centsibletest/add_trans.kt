package com.st10194321.centsibletest

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.util.Log
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
import com.google.firebase.firestore.Source
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

        // Spinner setup for categories
        categoryAdapter = ArrayAdapter(this,
            R.layout.spinner_item, categories).also {
            it.setDropDownViewResource(R.layout.spinner_dropdown_item)
            binding.spinnerCategory.adapter = it
        }

        // Accessibilty: https://developer.android.com/media/camera/camera-deprecated/photobasics#kotlin
        // Date: 01/05/2025
        // Image capture
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

        // Load categories
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

        // Add transaction
        binding.btnAddToCategory.setOnClickListener {
            val user = auth.currentUser
            if (user == null) {
                Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val name      = binding.etTxnName.text.toString().trim()
            val amountStr = binding.etTxnAmount.text.toString().trim()
            val details   = binding.etTxnDetails.text.toString().trim()
            val category  = binding.spinnerCategory.selectedItem.toString()
            val date      = binding.etTxnDate.text.toString().trim()

            // Validate
            if (name.isEmpty() || amountStr.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please complete all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0.0) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Build the map
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

            // Write it
            db.collection("users").document(user.uid)
                .collection("transactions")
                .add(txn)
                .addOnSuccessListener { docRef ->
                    Log.d("add_trans", "Local write OK, doc ID=${docRef.id}")
                    // Now wait for the server to confirm
                    db.waitForPendingWrites()
                        .addOnSuccessListener {
                            Log.d("add_trans", "Server ACK received")
                            Toast.makeText(this, "Transaction saved!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.e("add_trans", "Error syncing to server", e)
                            Toast.makeText(this, "Save error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("add_trans", "Local write failed", e)
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        // Back arrow
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    // Returns a Base64 string or null if no image was captured
    private fun convertImageToBase64(): String? {
        val bmp = capturedBitmap ?: return null
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 50, stream)
        val bytes = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }
}