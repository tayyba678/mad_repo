package com.example.unimatch

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore

class SignUp : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var signUpBtn: Button
    private lateinit var goLogin: TextView
    private lateinit var togglePassword: ImageView
    private lateinit var mainLayout: View
    private lateinit var logoCard: View
    private lateinit var signUpCard: View

    private var isPasswordVisible = false

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        signUpBtn = findViewById(R.id.signupBtn)
        goLogin = findViewById(R.id.goLogin)
        togglePassword = findViewById(R.id.togglePassword)
        mainLayout = findViewById(R.id.mainLayout)
        logoCard = findViewById(R.id.logoCard)
        signUpCard = findViewById(R.id.signUpCard)

        setupAnimations()
        setupListeners()
    }

    private fun setupAnimations() {
        mainLayout.alpha = 0f
        logoCard.translationY = -400f
        signUpCard.translationY = 800f

        mainLayout.animate().alpha(1f).setDuration(800).start()
        
        logoCard.animate()
            .translationY(0f)
            .setDuration(1200)
            .setInterpolator(OvershootInterpolator())
            .start()

        signUpCard.animate()
            .translationY(0f)
            .setDuration(1200)
            .setInterpolator(AnticipateOvershootInterpolator())
            .start()
    }

    private fun setupListeners() {
        // LIVE EMAIL VALIDATION
        email.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val regex = Regex("^[A-Za-z0-9._%+-]+@student\\.[a-zA-Z0-9-]+\\.edu\\.pk$")
                val text = s.toString()
                if (text.contains("@") && text.substringAfter("@").contains(".")) {
                    if (!regex.matches(text)) {
                        email.error = "Use university email format"
                    } else {
                        email.error = null
                    }
                } else {
                    email.error = null
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // PASSWORD TOGGLE
        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            password.transformationMethod = if (isPasswordVisible)
                HideReturnsTransformationMethod.getInstance()
            else
                PasswordTransformationMethod.getInstance()

            togglePassword.setImageResource(if (isPasswordVisible)
                R.drawable.is_eye_on
            else
                R.drawable.is_eye_off
            )
            password.setSelection(password.text.length)
        }

        // SIGNUP
        signUpBtn.setOnClickListener {
            val em = email.text.toString().trim()
            val pass = password.text.toString().trim()

            if (em.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val regex = Regex("^[A-Za-z0-9._%+-]+@student\\.[a-zA-Z0-9-]+\\.edu\\.pk$")
            if (!regex.matches(em)) {
                Toast.makeText(this, "Use valid university email format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            signUpBtn.isEnabled = false
            auth.createUserWithEmailAndPassword(em, pass)
                .addOnSuccessListener { result ->
                    val user = result.user
                    user?.sendEmailVerification()?.addOnSuccessListener {
                        val uid = user.uid
                        // Save initial pending status
                        db.collection("users").document(uid).set(
                            mapOf("uid" to uid, "email" to em, "status" to "PENDING")
                        )
                        
                        Toast.makeText(this, "Verification email sent", Toast.LENGTH_LONG).show()
                        
                        // REMOVED SIGN OUT HERE. Now user moves to verification screen while authenticated.
                        startActivity(Intent(this, EmailVerificationActivity::class.java))
                        finish()
                    }?.addOnFailureListener {
                        signUpBtn.isEnabled = true
                        Toast.makeText(this, "Failed to send verification email", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    signUpBtn.isEnabled = true
                    when (exception) {
                        is FirebaseAuthUserCollisionException -> Toast.makeText(this, "Account already exists. Please login.", Toast.LENGTH_LONG).show()
                        is FirebaseAuthWeakPasswordException -> Toast.makeText(this, "Password too weak", Toast.LENGTH_SHORT).show()
                        is FirebaseAuthInvalidCredentialsException -> Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(this, "Signup failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // GO TO LOGIN
        goLogin.setOnClickListener {
            startActivity(Intent(this, LoginScreen::class.java))
        }
    }
}
