package com.example.unimatch

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class User_registration : AppCompatActivity() {

    private lateinit var name: EditText
    private lateinit var regNo: EditText
    private lateinit var departmentSpinner: Spinner
    private lateinit var departmentManual: EditText
    private lateinit var admissionYearSpinner: Spinner
    private lateinit var phone: EditText
    private lateinit var genderGroup: RadioGroup
    private lateinit var submitBtn: Button

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // All UET departments
    private val departments = listOf(
        "Select Department",
        "Computer Science",
        "Software Engineering",
        "Electrical Engineering",
        "Electronics Engineering",
        "Mechanical Engineering",
        "Civil Engineering",
        "Chemical Engineering",
        "Petroleum & Gas Engineering",
        "Industrial & Manufacturing Engineering",
        "Materials Engineering",
        "Environmental Engineering",
        "Architecture",
        "City & Regional Planning",
        "Engineering Management",
        "Basic Sciences & Humanities",
        "Mathematics",
        "Physics",
        "Chemistry",
        "Other"
    )

    // Years from 1980 to 2030
    private val years = listOf("Select Year") +
            (1980..2030).map { it.toString() }.reversed()  // newest first

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_registration)

        name = findViewById(R.id.name)
        regNo = findViewById(R.id.regNo)
        departmentSpinner = findViewById(R.id.departmentSpinner)
        departmentManual = findViewById(R.id.department)
        admissionYearSpinner = findViewById(R.id.admissionYearSpinner)
        phone = findViewById(R.id.phone)
        genderGroup = findViewById(R.id.genderGroup)
        submitBtn = findViewById(R.id.submitBtn)

        setupDepartmentSpinner()
        setupYearSpinner()

        submitBtn.setOnClickListener {
            saveData()
        }
    }

    private fun setupDepartmentSpinner() {
        val adapter = ArrayAdapter(this, R.layout.spinner_item, departments)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        departmentSpinner.adapter = adapter

        departmentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                // Show manual input only if "Other" is selected
                if (departments[pos] == "Other") {
                    departmentManual.visibility = View.VISIBLE
                } else {
                    departmentManual.visibility = View.GONE
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupYearSpinner() {
        val adapter = ArrayAdapter(this, R.layout.spinner_item, years)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        admissionYearSpinner.adapter = adapter
    }

    private fun saveData() {

        val n = name.text.toString().trim()
        val r = regNo.text.toString().trim()
        val p = phone.text.toString().trim()

        // Department: use manual input if "Other" was selected
        val selectedDept = departmentSpinner.selectedItem.toString()
        val d = if (selectedDept == "Other") {
            departmentManual.text.toString().trim()
        } else {
            selectedDept
        }

        val y = admissionYearSpinner.selectedItem.toString()
        val selectedGenderId = genderGroup.checkedRadioButtonId

        // VALIDATION
        if (n.isEmpty()) { name.error = "Required"; return }
        if (r.isEmpty()) { regNo.error = "Required"; return }
        if (selectedDept == "Select Department") {
            Toast.makeText(this, "Please select a department", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedDept == "Other" && d.isEmpty()) {
            departmentManual.error = "Please type your department"
            return
        }
        if (y == "Select Year") {
            Toast.makeText(this, "Please select admission year", Toast.LENGTH_SHORT).show()
            return
        }
        if (p.isEmpty()) { phone.error = "Required"; return }
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show()
            return
        }

        val gender = findViewById<RadioButton>(selectedGenderId).text.toString()

        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginScreen::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }

        val uid = user.uid
        submitBtn.isEnabled = false

        val userMap = hashMapOf(
            "uid" to uid,
            "name" to n,
            "regNo" to r,
            "department" to d,
            "admissionYear" to y,
            "phone" to p,
            "gender" to gender
        )

        db.collection("users")
            .document(uid)
            .set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, InterestActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
            .addOnFailureListener { e ->
                submitBtn.isEnabled = true
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}