package com.st10194321.centsibletest

import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuView.ItemView

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView
import com.st10194321.centsibletest.databinding.ActivityMainBinding
import com.st10194321.centsibletest.databinding.ActivitySigninBinding


class MainActivity : AppCompatActivity() {




    private lateinit var binding: ActivityMainBinding

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContentView(R.layout.activity_main)

            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            binding.btnNewBudCat.setOnClickListener {
                val i = Intent(this, addBugCat::class.java)
                startActivity(i)
                finish()
            }
            binding.btnViewBudCat.setOnClickListener {
                val i = Intent(this, viewBugCat::class.java)
                startActivity(i)
                finish()
            }

            binding.btnTrackBudCat.setOnClickListener {
                val i = Intent(this, viewTrans::class.java)
                startActivity(i)
                finish()
            }
            binding.btnAddTrans.setOnClickListener {
                val i = Intent(this, add_trans::class.java)
                startActivity(i)
                finish()
            }



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
        }
    }

