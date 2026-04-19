package com.example.unimatch

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class UserProfile : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvDept: TextView
    private lateinit var tvPhone: TextView
    private lateinit var btnBack: Button
    private lateinit var btnMessage: Button

    private val db = FirebaseFirestore.getInstance()

    private var uid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        tvName = findViewById(R.id.tvName)
        tvDept = findViewById(R.id.tvDept)
        tvPhone = findViewById(R.id.tvPhone)
        btnBack = findViewById(R.id.btnBack)
        btnMessage = findViewById(R.id.btnMessage)

        uid = intent.getStringExtra("uid")

        if (uid == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadUser(uid!!)

        btnBack.setOnClickListener {
            finish()
        }

        btnMessage.setOnClickListener {
            finish() // already in chat context usually
        }
    }

    private fun loadUser(uid: String) {
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {

                    val name = doc.getString("name") ?: "Unknown"
                    val dept = doc.getString("department") ?: "Not set"
                    val phone = doc.getString("phone") ?: "Not available"

                    tvName.text = name
                    tvDept.text = dept
                    tvPhone.text = phone
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load user", Toast.LENGTH_SHORT).show()
            }
    }
}