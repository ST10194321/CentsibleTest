package com.st10194321.centsibletest

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.st10194321.centsibletest.databinding.ActivityAddTransBinding
import java.util.*

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import java.io.ByteArrayOutputStream


class add_trans : AppCompatActivity() {

    private lateinit var binding: ActivityAddTransBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private lateinit var categoryAdapter: ArrayAdapter<String>
    private val categories = mutableListOf<String>()

    private lateinit var captureImageButton: Button
    private lateinit var imageView: ImageView

    private var imageUri: Uri? = null
    private var capturedBitmap: Bitmap? = null
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
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

        // Set up category spinner
        categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter


        captureImageButton = findViewById(R.id.btnCaptureImage)
        imageView = findViewById(R.id.btnAddImage)

        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                capturedBitmap = bitmap
                imageView.setImageBitmap(bitmap)
            }
        }

        captureImageButton.setOnClickListener {
            cameraLauncher.launch(null)
        }



        // Load categories from Firestore
        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid)
                .collection("categories")
                .get()
                .addOnSuccessListener { result ->
                    for (doc in result) {
                        val cat = doc.getString("name")
                        if (cat != null) categories.add(cat)
                    }
                    categoryAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load categories", Toast.LENGTH_SHORT).show()
                }
        }

        // Date picker for transaction date
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

        // Add transaction button logic
        binding.btnAddToCategory.setOnClickListener {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val name = binding.etTxnName.text.toString().trim()
            val amountStr = binding.etTxnAmount.text.toString().trim()
            val details = binding.etTxnDetails.text.toString().trim()
            val selectedCategory = binding.spinnerCategory.selectedItem?.toString() ?: ""
            //val newCategory = binding.etNewCategory.text.toString().trim()
            val date = binding.etTxnDate.text.toString().trim()
            val imageBase64 = convertImageToBase64(imageView)
            //val imageBase64 = if (imageView.drawable != null) convertImageToBase64(imageView) else null


            //val finalCategory = if (newCategory.isNotEmpty()) newCategory else selectedCategory

            if (name.isEmpty() || amountStr.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please complete all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull()
            if (amount == null) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val transaction = mapOf(
                "name" to name,
                "amount" to amount,
                "details" to details,
                "category" to selectedCategory,
                "date" to date,
                "image" to imageBase64,
                "timestamp" to System.currentTimeMillis()
            )
//            if (imageBase64 != null) {
//                transaction["image"] = imageBase64
//            }

//            // Add new category if applicable
//            if (newCategory.isNotEmpty()) {
//                val categoryData = mapOf("name" to newCategory)
//                db.collection("users").document(currentUser.uid)
//                    .collection("categories")
//                    .add(categoryData)
//            }

            db.collection("users").document(currentUser.uid)
                .collection("transactions")
                .add(transaction)
                .addOnSuccessListener {
                    Toast.makeText(this, "Transaction added!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
    private fun convertImageToBase64(imageView: ImageView): String {
        val drawable = imageView.drawable ?: return ""
        val bitmap = (drawable as BitmapDrawable).bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
        val byteArray = stream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}
