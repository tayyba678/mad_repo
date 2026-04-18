package com.example.unimatch

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Profilee : AppCompatActivity() {

    private lateinit var name: EditText
    private lateinit var regNo: EditText
    private lateinit var department: EditText
    private lateinit var admissionYear: EditText
    private lateinit var phone: EditText
    private lateinit var updateBtn: Button

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profilee)

        name = findViewById(R.id.name)
        regNo = findViewById(R.id.regNo)
        department = findViewById(R.id.department)
        admissionYear = findViewById(R.id.admissionYear)
        phone = findViewById(R.id.phone)
        updateBtn = findViewById(R.id.updateBtn)

        val uid = auth.currentUser?.uid ?: return

        // 🔥 LOAD DATA FROM FIREBASE
        db.collection("users").document(uid).get()
            .addOnSuccessListener {

                if (it.exists()) {
                    name.setText(it.getString("name"))
                    regNo.setText(it.getString("regNo"))
                    department.setText(it.getString("department"))
                    admissionYear.setText(it.getString("admissionYear"))
                    phone.setText(it.getString("phone"))
                }
            }

        // 🔥 UPDATE DATA
        updateBtn.setOnClickListener {

            val updates = hashMapOf(
                "name" to name.text.toString(),
                "regNo" to regNo.text.toString(),
                "department" to department.text.toString(),
                "admissionYear" to admissionYear.text.toString(),
                "phone" to phone.text.toString()
            )

            db.collection("users").document(uid)
                .update(updates as Map<String, Any>)
                .addOnSuccessListener {

                    Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this, InterestActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
        }
    }
}