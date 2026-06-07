package com.example.unimatch

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : BaseActivity() {

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
                startActivity(Intent(this, EmailVerificationActivity::class.java))
                finish()
                return@postDelayed
            }

            // 3. Check if Profile Registration is complete
            db.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists() && document.contains("name")) {
                        startActivity(Intent(this, DashBoardActivity::class.java))
                    } else {
                        startActivity(Intent(this, User_registration::class.java))
                    }
                    finish()
                }
                .addOnFailureListener {
                    auth.signOut()
                    startActivity(Intent(this, LoginScreen::class.java))
                    finish()
                }

        }, 1500)
    }
}
