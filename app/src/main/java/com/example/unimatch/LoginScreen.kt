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

class LoginScreen : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginBtn: Button
    private lateinit var goRegister: TextView
    private lateinit var togglePassword: ImageView

    // track current visibility state
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)

        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        loginBtn = findViewById(R.id.loginBtn)
        goRegister = findViewById(R.id.goRegister)
        togglePassword = findViewById(R.id.togglePassword)

        val auth = FirebaseAuth.getInstance()

        // 👁 EYE TOGGLE
        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                // Show password
                password.transformationMethod = HideReturnsTransformationMethod.getInstance()
                togglePassword.setImageResource(R.drawable.is_eye_on)
            } else {
                // Hide password
                password.transformationMethod = PasswordTransformationMethod.getInstance()
                togglePassword.setImageResource(R.drawable.is_eye_off)
            }

            // Keep cursor at end of text
            password.setSelection(password.text.length)
        }

        // 🔐 LOGIN
        loginBtn.setOnClickListener {

            val em = email.text.toString().trim()
            val pass = password.text.toString().trim()

            if (em.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!em.endsWith("@uet.edu.pk")) {
                Toast.makeText(this, "Use university email only", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginBtn.isEnabled = false

            auth.signInWithEmailAndPassword(em, pass)
                .addOnSuccessListener {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener { exception ->
                    loginBtn.isEnabled = true
                    val message = when (exception) {
                        is FirebaseAuthInvalidUserException -> "Email not found"
                        is FirebaseAuthInvalidCredentialsException -> "Wrong password"
                        else -> "Login failed. Please try again"
                    }
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
        }

        // ➕ GO TO SIGNUP
        goRegister.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }
    }
}