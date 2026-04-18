package com.example.unimatch

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Handler(Looper.getMainLooper()).postDelayed({

            val user = auth.currentUser

            if (user == null) {

                startActivity(Intent(this, LoginScreen::class.java))
                finish()

            } else {

                val uid = user.uid

                db.collection("users").document(uid).get()
                    .addOnSuccessListener { document ->

                        if (document.exists()
                            && document.contains("name")
                            && document.contains("interests")
                        ) {

                            // ✅ FULL USER READY
                            startActivity(Intent(this, InterestActivity::class.java))

                        } else if (document.exists()) {

                            // ⚠️ profile exists but interests missing
                            startActivity(Intent(this, InterestActivity::class.java))

                        } else {

                            // ❌ no profile
                            startActivity(Intent(this, User_registration::class.java))
                        }

                        finish()
                    }
                    .addOnFailureListener {

                        // fallback
                        startActivity(Intent(this, LoginScreen::class.java))
                        finish()
                    }
            }

        }, 1500)
    }
}