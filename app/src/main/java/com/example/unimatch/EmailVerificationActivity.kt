package com.example.unimatch

import android.content.Intent
import android.os.*
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class EmailVerificationActivity : AppCompatActivity() {

    private lateinit var resendBtn: Button
    private lateinit var checkVerifyBtn: Button
    private lateinit var timerText: TextView
    private lateinit var goBack: TextView
    private lateinit var verificationCard: View

    private val auth = FirebaseAuth.getInstance()
    private var countdown = 30
    private val handler = Handler(Looper.getMainLooper())

    private val checkRunnable = object : Runnable {
        override fun run() {
            checkVerificationStatus(false)
            handler.postDelayed(this, 3000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_verification)

        resendBtn = findViewById(R.id.resendBtn)
        checkVerifyBtn = findViewById(R.id.checkVerifyBtn)
        timerText = findViewById(R.id.timerText)
        goBack = findViewById(R.id.goBack)
        verificationCard = findViewById(R.id.verificationCard)

        setupAnimations()
        startTimer()
        handler.post(checkRunnable)

        // Force check when user clicks button
        checkVerifyBtn.setOnClickListener {
            checkVerificationStatus(true)
        }

        // Resend email logic
        resendBtn.setOnClickListener {
            auth.currentUser?.sendEmailVerification()?.addOnSuccessListener {
                Toast.makeText(this, "Verification email resent", Toast.LENGTH_SHORT).show()
                startTimer()
            }?.addOnFailureListener {
                Toast.makeText(this, "Failed to resend: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Go back if email is wrong
        goBack.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, SignUp::class.java))
            finish()
        }
    }

    private fun setupAnimations() {
        verificationCard.translationY = 500f
        verificationCard.alpha = 0f
        verificationCard.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(1000)
            .setInterpolator(OvershootInterpolator())
            .start()
    }

    private fun checkVerificationStatus(manual: Boolean) {
        val user = auth.currentUser
        if (user == null) {
            handler.removeCallbacks(checkRunnable)
            return
        }

        user.reload().addOnSuccessListener {
            if (user.isEmailVerified) {
                handler.removeCallbacks(checkRunnable)
                Toast.makeText(this, "Email Verified! Please Login.", Toast.LENGTH_SHORT).show()
                
                // Sign out as requested, so they have to login
                auth.signOut()
                
                val intent = Intent(this, LoginScreen::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else if (manual) {
                Toast.makeText(this, "Still not verified. Please check your inbox.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            if (manual) Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startTimer() {
        countdown = 30
        resendBtn.isEnabled = false
        resendBtn.alpha = 0.5f

        val timerHandler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (countdown > 0) {
                    timerText.text = "Resend available in $countdown sec"
                    countdown--
                    timerHandler.postDelayed(this, 1000)
                } else {
                    resendBtn.isEnabled = true
                    resendBtn.alpha = 1.0f
                    timerText.text = "You can resend now"
                }
            }
        }
        timerHandler.post(runnable)
    }

    override fun onDestroy() {
        handler.removeCallbacks(checkRunnable)
        super.onDestroy()
    }
}
