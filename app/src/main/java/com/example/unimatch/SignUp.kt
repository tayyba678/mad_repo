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

class SignUp : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var signUpBtn: Button
    private lateinit var togglePassword: ImageView

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        signUpBtn = findViewById(R.id.signupBtn)
        togglePassword = findViewById(R.id.togglePassword)

        val auth = FirebaseAuth.getInstance()

        // 👁 EYE TOGGLE
        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                password.transformationMethod = HideReturnsTransformationMethod.getInstance()
                togglePassword.setImageResource(R.drawable.is_eye_on)
            } else {
                password.transformationMethod = PasswordTransformationMethod.getInstance()
                togglePassword.setImageResource(R.drawable.is_eye_off)
            }

            // Keep cursor at end of text
            password.setSelection(password.text.length)
        }

        signUpBtn.setOnClickListener {

            val em = email.text.toString().trim()
            val pass = password.text.toString().trim()

            if (em.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!em.endsWith("@uet.edu.pk")) {
                Toast.makeText(this, "Only UET email allowed", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            signUpBtn.isEnabled = false

            auth.createUserWithEmailAndPassword(em, pass)
                .addOnSuccessListener {
                    Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, User_registration::class.java))
                    finish()
                }
                .addOnFailureListener { exception ->
                    signUpBtn.isEnabled = true
                    val message = when (exception) {
                        is FirebaseAuthUserCollisionException -> "Email already registered"
                        is FirebaseAuthWeakPasswordException -> "Password is too weak"
                        is FirebaseAuthInvalidCredentialsException -> "Invalid email format"
                        else -> "Signup failed. Please try again"
                    }
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
        }
    }
}