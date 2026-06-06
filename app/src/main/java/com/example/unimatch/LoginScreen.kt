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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginScreen : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginBtn: Button
    private lateinit var goRegister: TextView
    private lateinit var forgotPassword: TextView
    private lateinit var togglePassword: ImageView
    private lateinit var mainLayout: View
    private lateinit var logoCard: View
    private lateinit var loginCard: View

    private var isPasswordVisible = false
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)

        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        loginBtn = findViewById(R.id.loginBtn)
        goRegister = findViewById(R.id.goRegister)
        forgotPassword = findViewById(R.id.forgotPassword)
        togglePassword = findViewById(R.id.togglePassword)
        mainLayout = findViewById(R.id.mainLayout)
        logoCard = findViewById(R.id.logoCard)
        loginCard = findViewById(R.id.loginCard)

        setupAnimations()
        setupListeners()
    }

    private fun setupAnimations() {
        mainLayout.alpha = 0f
        logoCard.translationY = -400f
        loginCard.translationY = 800f

        mainLayout.animate().alpha(1f).setDuration(800).start()

        logoCard.animate()
            .translationY(0f)
            .setDuration(1200)
            .setInterpolator(OvershootInterpolator())
            .start()

        loginCard.animate()
            .translationY(0f)
            .setDuration(1200)
            .setInterpolator(AnticipateOvershootInterpolator())
            .start()
    }

    private fun setupListeners() {
        email.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val regex = Regex("^[A-Za-z0-9._%+-]+@student\\.[a-zA-Z0-9-]+\\.edu\\.pk$")
                if (!regex.matches(s.toString())) {
                    email.error = "Use university email"
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            password.transformationMethod = if (isPasswordVisible)
                HideReturnsTransformationMethod.getInstance()
            else
                PasswordTransformationMethod.getInstance()

            togglePassword.setImageResource(if (isPasswordVisible) R.drawable.is_eye_on else R.drawable.is_eye_off)
            password.setSelection(password.text.length)
        }

        loginBtn.setOnClickListener {
            val em = email.text.toString().trim()
            val pass = password.text.toString().trim()

            if (em.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginBtn.isEnabled = false
            auth.signInWithEmailAndPassword(em, pass)
                .addOnSuccessListener {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        auth.signOut()
                        loginBtn.isEnabled = true
                        Toast.makeText(this, "Please verify your email first", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener { exception ->
                    loginBtn.isEnabled = true
                    val message = when (exception) {
                        is FirebaseAuthInvalidUserException -> "Email not found"
                        is FirebaseAuthInvalidCredentialsException -> "Wrong password"
                        else -> "Login failed"
                    }
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
        }

        goRegister.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }

        forgotPassword.setOnClickListener {
            val em = email.text.toString().trim()
            if (em.isEmpty()) {
                Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.sendPasswordResetEmail(em).addOnSuccessListener {
                Toast.makeText(this, "Reset email sent", Toast.LENGTH_LONG).show()
            }
        }
    }
}
