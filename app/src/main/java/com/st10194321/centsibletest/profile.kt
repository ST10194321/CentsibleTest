package com.st10194321.centsibletest

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.st10194321.centsibletest.databinding.ActivityMainBinding
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
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val db = FirebaseFirestore.getInstance()

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



        // Initialize views

        binding.iconHome.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }
        // Handle Edit Profile click
        binding.tvEditProfile.setOnClickListener {

            val i = Intent(this, editprofile::class.java)
            startActivity(i)

        }

        // Handle Set Goals click
        binding.cardViewGoals.setOnClickListener {
            startActivity(Intent(this, viewgoals::class.java))
        }

        // Handle Set Income click
        binding.iconHome.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }
//            catsIcon.setOnClickListener {
//                val i = Intent(this, categories::class.java)
//                startActivity(i)
//                finish()
//           }
//            reportsIcon.setOnClickListener {
//                val i = Intent(this, reports::class.java)
//                startActivity(i)
//                finish()
//            }

        binding.iconProfile.setOnClickListener {
            val i = Intent(this, profile::class.java)
            startActivity(i)
            finish()
        }

        binding.cardLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, signin::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->
            if (bitmap != null) {
                capturedBitmap = bitmap
                binding.profileImage.setImageBitmap(bitmap)

                // Upload to Firestore after image is captured
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

        binding.profileImage.setOnClickListener {
            cameraLauncher.launch(null)
        }


    }

    private fun convertImageToBase64(): String? {
        val bmp = capturedBitmap ?: return null
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 50, stream)
        val bytes = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }


}
