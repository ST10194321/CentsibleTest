package com.st10194321.centsibletest

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.st10194321.centsibletest.databinding.ActivityProfileBinding
import java.io.ByteArrayOutputStream

class profile : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var capturedBitmap: Bitmap? = null
    private lateinit var cameraLauncher: ActivityResultLauncher<Void?>
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val uid = auth.currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

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
        binding.achievementsLabel.setOnClickListener{
            startActivity(Intent(this, achievements::class.java))
            finish()
        }
        binding.iconCategories.setOnClickListener {
            startActivity(Intent(this, viewBugCat::class.java))
            finish()
        }

        binding.iconProfile.setOnClickListener {
            startActivity(Intent(this, profile::class.java))
            finish()
        }

        // Edit profile screen
        binding.tvEditProfile.setOnClickListener {
            startActivity(Intent(this, editprofile::class.java))
        }

        // View goals screen
        binding.cardViewGoals.setOnClickListener {
            startActivity(Intent(this, viewgoals::class.java))
        }

        // Logout
        binding.cardLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, signin::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Accessibilty: https://developer.android.com/media/camera/camera-deprecated/photobasics#kotlin
        // Date: 01/05/2025
        // Set up camera to take profile picture
        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->
            if (bitmap != null) {
                capturedBitmap = bitmap
                binding.profileImage.setImageBitmap(bitmap)

                // Upload captured image to Firestore
                val base64 = convertImageToBase64()
                if (base64 != null) {
                    val txn = mapOf("image" to base64)
                    db.collection("users").document(uid)
                        .set(txn, SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Profile Image added", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, profile::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
            }
        }

        // Launch camera on profile image click
        binding.profileImage.setOnClickListener {
            cameraLauncher.launch(null)
        }
    }

    //    Author: Mughira Dar
    //    Accessibilty: https://stackoverflow.com/questions/58955434/how-to-convert-base64-string-into-image-in-kotlin-android
    //    Date: 28/04/2025
    // Helper method to convert bitmap to base64 string
    private fun convertImageToBase64(): String? {
        val bmp = capturedBitmap ?: return null
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 50, stream)
        val bytes = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }
}

