package com.st10194321.centsibletest

import CategoryAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.st10194321.centsibletest.databinding.ActivityViewBugCatBinding


class viewBugCat : AppCompatActivity() {
    companion object {
        const val EXTRA_CATEGORY = "EXTRA_CATEGORY"
    }
    // view binding
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

        //Author: Firebase Documentation Team
        //Accessibiltiy: https://firebase.google.com/docs/firestore/query-data/get-data?platform=android
        //Date Accessed: 20/04/2025



        val uid = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(uid)
            .collection("categories")
            .get()
            .addOnSuccessListener { snap ->

                val list = snap.documents.mapNotNull {
                    it.toObject(Category::class.java)
                }.toMutableList()

                // Add a dummy category for "All Transactions"
                val allTransactionsCategory = Category(name = "All Transactions")
                list.add(0, allTransactionsCategory)

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


        //leads to the add category page
        binding.btnAddCategory.setOnClickListener {
            startActivity(Intent(this, addBugCat::class.java))
        }

        //leads back to the main page
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }


    }
    }



