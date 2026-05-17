package com.example.unimatch

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.FirebaseFirestore

class SignUp : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var signUpBtn: Button
    private lateinit var goLogin: TextView
    private lateinit var togglePassword: ImageView

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        signUpBtn = findViewById(R.id.signupBtn)
        togglePassword = findViewById(R.id.togglePassword)

        // Wire up password eye toggle
        togglePassword.setOnClickListener { togglePasswordVisibility() }

        signUpBtn.setOnClickListener { attemptSignUp() }

        // Optional: add a "Already have account? Login" link if your XML has it
        // goLogin = findViewById(R.id.goLogin)
        // goLogin.setOnClickListener { startActivity(Intent(this, LoginScreen::class.java)) }
    }

    // Toggles password field between visible and hidden
    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        password.transformationMethod = if (isPasswordVisible)
            HideReturnsTransformationMethod.getInstance()
        else
            PasswordTransformationMethod.getInstance()
        togglePassword.setImageResource(
            if (isPasswordVisible) R.drawable.is_eye_on else R.drawable.is_eye_off
        )
        password.setSelection(password.text.length)
    }

    // Validates fields locally then creates Firebase account
    private fun attemptSignUp() {
        val em = email.text.toString().trim()
        val pass = password.text.toString().trim()

        // Local validation — no network call made until all pass
        if (em.isEmpty()) {
            email.error = "Email is required"
            return
        }
        if (!em.endsWith(AppConstants.UNIVERSITY_DOMAIN)) {
            email.error = "Use your university email (${AppConstants.UNIVERSITY_DOMAIN})"
            return
        }
        if (pass.isEmpty()) {
            password.error = "Password is required"
            return
        }
        if (pass.length < 6) {
            password.error = "Password must be at least 6 characters"
            return
        }

        signUpBtn.isEnabled = false

        // Step 1: Create Firebase Auth account
        auth.createUserWithEmailAndPassword(em, pass)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: run {
                    showError("Account creation failed. Try again.")
                    signUpBtn.isEnabled = true
                    return@addOnSuccessListener
                }

                // Step 2: Send verification email before doing anything else
                authResult.user?.sendEmailVerification()
                    ?.addOnSuccessListener {

                        // Step 3: Write partial Firestore document — flags both false until verified
                        writePartialUserDocument(uid, em)
                    }
                    ?.addOnFailureListener {
                        // Email send failed but account exists — still proceed, user can resend
                        writePartialUserDocument(uid, em)
                    }
            }
            .addOnFailureListener { exception ->
                signUpBtn.isEnabled = true
                val message = when (exception) {
                    is FirebaseAuthUserCollisionException -> "This email is already registered"
                    is FirebaseAuthWeakPasswordException -> "Password is too weak"
                    is FirebaseAuthInvalidCredentialsException -> "Invalid email format"
                    else -> "Sign up failed. Please try again."
                }
                showError(message)
            }
    }

    // Writes initial user document with both flags false — profile not complete yet
    private fun writePartialUserDocument(uid: String, emailAddress: String) {
        val partialUser = hashMapOf(
            AppConstants.FIELD_EMAIL to emailAddress,
            AppConstants.FIELD_IS_VERIFIED to false,
            AppConstants.FIELD_PROFILE_COMPLETE to false,
            AppConstants.FIELD_CREATED_AT to System.currentTimeMillis()
        )

        db.collection(AppConstants.COLLECTION_USERS)
            .document(uid)
            .set(partialUser)
            .addOnSuccessListener {
                // Navigate to email verification gate
                startActivity(Intent(this, EmailVerification::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
            .addOnFailureListener {
                signUpBtn.isEnabled = true
                showError("Failed to save account data. Please try again.")
            }
    }

    // Shows a toast error message
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}