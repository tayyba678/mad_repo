package com.example.unimatch

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class User_registration : AppCompatActivity() {

    private lateinit var name: EditText
    private lateinit var regNo: EditText
    private lateinit var department: EditText
    private lateinit var admissionYear: EditText
    private lateinit var phone: EditText
    private lateinit var genderGroup: RadioGroup
    private lateinit var submitBtn: Button

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_registration)


        name = findViewById(R.id.name)
        regNo = findViewById(R.id.regNo)
        department = findViewById(R.id.department)
        admissionYear = findViewById(R.id.admissionYear)
        phone = findViewById(R.id.phone)
        genderGroup = findViewById(R.id.genderGroup)
        submitBtn = findViewById(R.id.submitBtn)

        submitBtn.setOnClickListener {
            saveData()
        }
    }

    private fun saveData() {

        val n = name.text.toString().trim()
        val r = regNo.text.toString().trim()
        val d = department.text.toString().trim()
        val y = admissionYear.text.toString().trim()
        val p = phone.text.toString().trim()

        val selectedGenderId = genderGroup.checkedRadioButtonId

        // ✅ VALIDATION
        if (n.isEmpty()) { name.error = "Required"; return }
        if (r.isEmpty()) { regNo.error = "Required"; return }
        if (d.isEmpty()) { department.error = "Required"; return }
        if (y.isEmpty()) { admissionYear.error = "Required"; return }
        if (p.isEmpty()) { phone.error = "Required"; return }

        if (selectedGenderId == -1) {
            Toast.makeText(this, "Select gender", Toast.LENGTH_SHORT).show()
            return
        }

        val gender = findViewById<RadioButton>(selectedGenderId).text.toString()

        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = user.uid   // 👈 UNIQUE ID (VERY IMPORTANT)

        // 🔥 FIREBASE DATA MAP
        val userMap = hashMapOf(
            "uid" to uid,
            "name" to n,
            "regNo" to r,
            "department" to d,
            "admissionYear" to y,
            "phone" to p,
            "gender" to gender
        )

        // 🔥 SAVE DATA IN FIRESTORE
        db.collection("users")
            .document(uid)
            .set(userMap)
            .addOnSuccessListener {

                Toast.makeText(this, "Profile Saved", Toast.LENGTH_SHORT).show()

                // 👉 NEXT SCREEN
                val intent = Intent(this, InterestActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}