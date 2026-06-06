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

            // 1. Check if user is logged in
            if (user == null) {
                startActivity(Intent(this, LoginScreen::class.java))
                finish()
                return@postDelayed
            }

            // 2. Check if email is verified
            if (!user.isEmailVerified) {
                // If not verified, they must go through the verification flow again
                startActivity(Intent(this, EmailVerificationActivity::class.java))
                finish()
                return@postDelayed
            }

            // 3. Check if Profile Registration is complete
            db.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    // Check if 'name' exists - if yes, profile is complete
                    if (document.exists() && document.contains("name")) {
                        startActivity(Intent(this, DashBoardActivity::class.java))
                    } else {
                        // User exists but profile isn't filled yet
                        startActivity(Intent(this, User_registration::class.java))
                    }
                    finish()
                }
                .addOnFailureListener {
                    // Fallback to Login on error
                    auth.signOut()
                    startActivity(Intent(this, LoginScreen::class.java))
                    finish()
                }

        }, 1500)
    }
}
