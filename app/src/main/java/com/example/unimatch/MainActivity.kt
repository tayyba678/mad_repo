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

        // Show splash for 2 seconds then run the 3-gate routing check
        Handler(Looper.getMainLooper()).postDelayed({
            routeUser()
        }, AppConstants.SPLASH_DELAY_MS)
    }

    // 3-gate router: gate 1 = signed in, gate 2 = email verified, gate 3 = profile complete
    private fun routeUser() {
        val user = auth.currentUser

        // Gate 1: no Firebase Auth user at all → Sign Up
        if (user == null) {
            goTo(SignUp::class.java)
            return
        }

        // Reload to get the latest isEmailVerified status from Firebase servers
        user.reload().addOnCompleteListener { reloadTask ->

            // Gate 2: email not verified → Email Verification screen
            if (!user.isEmailVerified) {
                goTo(EmailVerification::class.java)
                return@addOnCompleteListener
            }

            // Gate 3: check Firestore for profileComplete flag
            db.collection(AppConstants.COLLECTION_USERS)
                .document(user.uid)
                .get()
                .addOnSuccessListener { doc ->
                    val profileComplete = doc.getBoolean(AppConstants.FIELD_PROFILE_COMPLETE) ?: false

                    if (!doc.exists() || !profileComplete) {
                        // Profile not finished → Profile Setup
                        goTo(User_registration::class.java)
                    } else {
                        // All 3 gates passed → Main app (Interest/Match screen)
                        goTo(InterestActivity::class.java)
                    }
                }
                .addOnFailureListener {
                    // Firestore read failed — safe fallback is login screen
                    goTo(LoginScreen::class.java)
                }
        }.addOnFailureListener {
            // Reload failed (no internet) — check cached value
            if (!user.isEmailVerified) {
                goTo(EmailVerification::class.java)
            } else {
                // Trust cached Firestore if available, else send to login
                goTo(LoginScreen::class.java)
            }
        }
    }

    // Navigates to a screen and clears the back stack so user cannot go back to splash
    private fun goTo(destination: Class<*>) {
        startActivity(Intent(this, destination).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}