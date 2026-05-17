package com.example.unimatch

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class User_registration : AppCompatActivity() {

    private lateinit var nameField: EditText
    private lateinit var regNoField: EditText
    private lateinit var departmentSpinner: Spinner
    private lateinit var departmentManual: EditText
    private lateinit var admissionYearSpinner: Spinner
    private lateinit var phoneField: EditText
    private lateinit var genderGroup: RadioGroup
    private lateinit var submitBtn: Button

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

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

    private val years = listOf("Select Year") +
            (1980..2030).map { it.toString() }.reversed()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_registration)

        nameField = findViewById(R.id.name)
        regNoField = findViewById(R.id.regNo)
        departmentSpinner = findViewById(R.id.departmentSpinner)
        departmentManual = findViewById(R.id.department)
        admissionYearSpinner = findViewById(R.id.admissionYearSpinner)
        phoneField = findViewById(R.id.phone)
        genderGroup = findViewById(R.id.genderGroup)
        submitBtn = findViewById(R.id.submitBtn)

        setupDepartmentSpinner()
        setupYearSpinner()

        submitBtn.setOnClickListener { saveProfile() }
    }

    // Sets up department dropdown with "Other" revealing a free-text field
    private fun setupDepartmentSpinner() {
        val adapter = ArrayAdapter(this, R.layout.spinner_item, departments)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        departmentSpinner.adapter = adapter

        departmentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                departmentManual.visibility =
                    if (departments[pos] == "Other") View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // Sets up admission year dropdown newest-first
    private fun setupYearSpinner() {
        val adapter = ArrayAdapter(this, R.layout.spinner_item, years)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        admissionYearSpinner.adapter = adapter
    }

    // Validates all fields and writes complete profile to Firestore with profileComplete flag
    private fun saveProfile() {
        val name = nameField.text.toString().trim()
        val regNo = regNoField.text.toString().trim()
        val phone = phoneField.text.toString().trim()
        val selectedDept = departmentSpinner.selectedItem.toString()
        val department = if (selectedDept == "Other") {
            departmentManual.text.toString().trim()
        } else {
            selectedDept
        }
        val admissionYear = admissionYearSpinner.selectedItem.toString()
        val selectedGenderId = genderGroup.checkedRadioButtonId

        // Field validation — stop on first error
        if (name.isEmpty()) { nameField.error = "Required"; return }
        if (regNo.isEmpty()) { regNoField.error = "Required"; return }
        if (selectedDept == "Select Department") {
            Toast.makeText(this, "Please select your department", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedDept == "Other" && department.isEmpty()) {
            departmentManual.error = "Please enter your department"
            return
        }
        if (admissionYear == "Select Year") {
            Toast.makeText(this, "Please select admission year", Toast.LENGTH_SHORT).show()
            return
        }
        if (phone.isEmpty()) { phoneField.error = "Required"; return }
        if (phone.length < 10) { phoneField.error = "Enter a valid phone number"; return }
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show()
            return
        }

        val gender = findViewById<RadioButton>(selectedGenderId).text.toString()

        val user = auth.currentUser ?: run {
            // Session expired — send back to sign up
            Toast.makeText(this, "Session expired. Please sign up again.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, SignUp::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }

        submitBtn.isEnabled = false

        // Build the complete user document — profileComplete and isVerified both true here
        val userDocument = hashMapOf(
            AppConstants.FIELD_NAME to name,
            AppConstants.FIELD_EMAIL to (user.email ?: ""),
            AppConstants.FIELD_ROLL_NUMBER to regNo,
            AppConstants.FIELD_DEPARTMENT to department,
            AppConstants.FIELD_ADMISSION_YEAR to admissionYear,
            AppConstants.FIELD_PHONE to phone,
            "gender" to gender,
            AppConstants.FIELD_IS_VERIFIED to true,
            AppConstants.FIELD_PROFILE_COMPLETE to true,
            AppConstants.FIELD_CURRENT_MATCH_ID to null,
            "matchHistory" to emptyList<String>(),
            AppConstants.FIELD_CREATED_AT to System.currentTimeMillis()
        )

        // Merge so we overwrite without losing the email/createdAt from the partial doc
        db.collection(AppConstants.COLLECTION_USERS)
            .document(user.uid)
            .set(userDocument)
            .addOnSuccessListener {
                // Profile complete — enter the main app
                startActivity(Intent(this, InterestActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
            .addOnFailureListener { e ->
                submitBtn.isEnabled = true
                Toast.makeText(this, "Failed to save profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}