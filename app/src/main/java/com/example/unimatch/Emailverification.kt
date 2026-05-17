package com.example.unimatch

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class EmailVerification : AppCompatActivity() {

    private lateinit var tvInstruction: TextView
    private lateinit var btnVerified: Button
    private lateinit var btnResend: Button
    private lateinit var tvResendTimer: TextView

    private val auth = FirebaseAuth.getInstance()
    private var resendTimer: CountDownTimer? = null

    // Renamed: no underscores in private val names per Kotlin convention
    private val resendCooldownMs = 60_000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_verification)

        tvInstruction = findViewById(R.id.tvInstruction)
        btnVerified = findViewById(R.id.btnVerified)
        btnResend = findViewById(R.id.btnResend)
        tvResendTimer = findViewById(R.id.tvResendTimer)

        // Use getString with placeholder — never concatenate directly into setText
        val email = auth.currentUser?.email ?: getString(R.string.ev_fallback_email)
        tvInstruction.text = getString(R.string.ev_instruction, email)

        // Start cooldown immediately — email was sent from SignUp before arriving here
        startResendCooldown()

        btnVerified.setOnClickListener { checkVerificationStatus() }
        btnResend.setOnClickListener { resendVerificationEmail() }

        // Migrate from deprecated onBackPressed to OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Sign out and return to SignUp — back cannot bypass the verification gate
                auth.signOut()
                goTo(SignUp::class.java)
            }
        })
    }

    // Reloads user from Firebase servers and checks isEmailVerified
    private fun checkVerificationStatus() {
        val user = auth.currentUser ?: run {
            goTo(SignUp::class.java)
            return
        }

        btnVerified.isEnabled = false
        btnVerified.text = getString(R.string.ev_btn_checking)

        // Must call reload() — local FirebaseUser object caches isEmailVerified and won't update on its own
        user.reload()
            .addOnSuccessListener {
                if (user.isEmailVerified) {
                    goTo(User_registration::class.java)
                } else {
                    btnVerified.isEnabled = true
                    btnVerified.text = getString(R.string.ev_btn_verified)
                    Toast.makeText(this, getString(R.string.ev_not_verified_yet), Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                btnVerified.isEnabled = true
                btnVerified.text = getString(R.string.ev_btn_verified)
                Toast.makeText(this, getString(R.string.ev_check_failed), Toast.LENGTH_SHORT).show()
            }
    }

    // Sends a new Firebase verification email to the current user
    private fun resendVerificationEmail() {
        val user = auth.currentUser ?: return
        btnResend.isEnabled = false

        user.sendEmailVerification()
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.ev_resend_success), Toast.LENGTH_SHORT).show()
                startResendCooldown()
            }
            .addOnFailureListener {
                btnResend.isEnabled = true
                Toast.makeText(this, getString(R.string.ev_resend_failed), Toast.LENGTH_SHORT).show()
            }
    }

    // Disables the resend button for 60 seconds and shows a live countdown
    private fun startResendCooldown() {
        btnResend.isEnabled = false
        tvResendTimer.visibility = View.VISIBLE

        resendTimer?.cancel()
        resendTimer = object : CountDownTimer(resendCooldownMs, 1000) {
            override fun onTick(millisRemaining: Long) {
                val seconds = millisRemaining / 1000
                // Use getString with placeholder — not string concatenation
                tvResendTimer.text = getString(R.string.ev_timer, seconds)
            }

            override fun onFinish() {
                btnResend.isEnabled = true
                tvResendTimer.visibility = View.GONE
            }
        }.start()
    }

    // Navigates to destination and clears the back stack
    private fun goTo(destination: Class<*>) {
        startActivity(Intent(this, destination).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel timer to prevent memory leak when activity is destroyed
        resendTimer?.cancel()
    }
}