package com.example.unimatch

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.FirebaseFirestore

class LoginScreen : AppCompatActivity() {

    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginBtn: Button
    private lateinit var goRegister: TextView
    private lateinit var togglePassword: ImageView

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)

        emailField = findViewById(R.id.email)
        passwordField = findViewById(R.id.password)
        loginBtn = findViewById(R.id.loginBtn)
        goRegister = findViewById(R.id.goRegister)
        togglePassword = findViewById(R.id.togglePassword)

        togglePassword.setOnClickListener { togglePasswordVisibility() }

        loginBtn.setOnClickListener { attemptLogin() }

        goRegister.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }
    }

    // Toggles password field between visible and hidden
    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        passwordField.transformationMethod = if (isPasswordVisible)
            HideReturnsTransformationMethod.getInstance()
        else
            PasswordTransformationMethod.getInstance()
        togglePassword.setImageResource(
            if (isPasswordVisible) R.drawable.is_eye_on else R.drawable.is_eye_off
        )
        passwordField.setSelection(passwordField.text.length)
    }

    // Validates locally, authenticates, then runs the same 3-gate check as MainActivity
    private fun attemptLogin() {
        val em = emailField.text.toString().trim()
        val pass = passwordField.text.toString().trim()

        // Local validation
        if (em.isEmpty()) { emailField.error = "Email is required"; return }
        if (!em.endsWith(AppConstants.UNIVERSITY_DOMAIN)) {
            emailField.error = "Use your university email"
            return
        }
        if (pass.isEmpty()) { passwordField.error = "Password is required"; return }

        loginBtn.isEnabled = false

        auth.signInWithEmailAndPassword(em, pass)
            .addOnSuccessListener {
                // Auth succeeded — now run the same 3-gate routing logic
                routeAfterLogin()
            }
            .addOnFailureListener { exception ->
                loginBtn.isEnabled = true
                val message = when (exception) {
                    is FirebaseAuthInvalidUserException -> "No account found with this email"
                    is FirebaseAuthInvalidCredentialsException -> "Incorrect password"
                    else -> "Login failed. Please try again."
                }
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
    }

    // After login, re-runs the 3-gate check — same logic as MainActivity
    private fun routeAfterLogin() {
        val user = auth.currentUser ?: run {
            loginBtn.isEnabled = true
            return
        }

        // Reload to get fresh isEmailVerified from server
        user.reload().addOnCompleteListener {

            // Gate 2: email not verified
            if (!user.isEmailVerified) {
                startActivity(Intent(this, EmailVerification::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
                return@addOnCompleteListener
            }

            // Gate 3: profileComplete in Firestore
            db.collection(AppConstants.COLLECTION_USERS)
                .document(user.uid)
                .get()
                .addOnSuccessListener { doc ->
                    loginBtn.isEnabled = true
                    val profileComplete = doc.getBoolean(AppConstants.FIELD_PROFILE_COMPLETE) ?: false

                    if (!doc.exists() || !profileComplete) {
                        startActivity(Intent(this, User_registration::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                    } else {
                        startActivity(Intent(this, InterestActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                    }
                    finish()
                }
                .addOnFailureListener {
                    loginBtn.isEnabled = true
                    Toast.makeText(this, "Could not load profile. Try again.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}