package com.st10194321.centsibletest

import CategoryAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.st10194321.centsibletest.databinding.ActivityViewBugCatBinding


class viewBugCat : AppCompatActivity() {
    companion object {
        const val EXTRA_CATEGORY = "EXTRA_CATEGORY"
    }
    private lateinit var binding: ActivityViewBugCatBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db   by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewBugCatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()



        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(this@viewBugCat)
        }


        val uid = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(uid)
            .collection("categories")
            .get()
            .addOnSuccessListener { snap ->

                val list = snap.documents.mapNotNull {
                    it.toObject(Category::class.java)
                }

                binding.rvCategories.adapter = CategoryAdapter(list) { cat ->
                    Toast.makeText(this, "Clicked ${cat.name}", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, viewTrans::class.java).apply {
                        putExtra(EXTRA_CATEGORY, cat.name)
                    }
                    startActivity(intent)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load: ${e.message}", Toast.LENGTH_SHORT).show()
            }


        binding.btnAddCategory.setOnClickListener {
            startActivity(Intent(this, addBugCat::class.java))
        }

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }


    }
    }



