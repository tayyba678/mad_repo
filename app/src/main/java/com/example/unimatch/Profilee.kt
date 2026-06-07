package com.example.unimatch

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Profilee : BaseActivity() {

    private lateinit var nameField: EditText
    private lateinit var departmentSpinner: Spinner
    private lateinit var admissionYearSpinner: Spinner
    private lateinit var phoneField: EditText
    private lateinit var updateBtn: Button
    private lateinit var nameInitial: TextView
    private lateinit var headerName: TextView
    private lateinit var headerRegNo: TextView
    private lateinit var profileLayout: View
    private lateinit var infoCard: View

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var originalName = ""
    private var originalDept = ""
    private var originalYear = ""
    private var originalPhone = ""
    private var userRegNo = ""

    private val departments = listOf(
        "Architecture", "Artificial Intelligence", "Biology", "Business Administration",
        "Chemical Engineering", "Chemistry", "Civil Engineering", "Computer Science",
        "Data Science", "Economics", "Electrical Engineering", "Electronics Engineering",
        "Engineering Management", "Environmental Engineering", "Fine Arts",
        "Geological Engineering", "Industrial Engineering", "Information Technology",
        "Mathematics", "Mechanical Engineering", "Mechatronics Engineering",
        "Metallurgical Engineering", "Mining Engineering", "Petroleum Engineering",
        "Physics", "Polymer Engineering", "Software Engineering", "Textile Engineering", "Other"
    ).sorted()

    private val years = listOf("Select Year") + (1980..2030).map { it.toString() }.reversed()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profilee)

        nameField = findViewById(R.id.name)
        departmentSpinner = findViewById(R.id.departmentSpinner)
        admissionYearSpinner = findViewById(R.id.admissionYearSpinner)
        phoneField = findViewById(R.id.phone)
        updateBtn = findViewById(R.id.updateBtn)
        nameInitial = findViewById(R.id.nameInitial)
        headerName = findViewById(R.id.headerName)
        headerRegNo = findViewById(R.id.headerRegNo)
        profileLayout = findViewById(R.id.profileLayout)
        infoCard = findViewById(R.id.infoCard)

        setupSpinners()
        setupAnimations()
        loadProfile()

        nameField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val firstChar = s?.toString()?.trim()?.firstOrNull()?.uppercase() ?: "U"
                nameInitial.text = firstChar
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        updateBtn.setOnClickListener { validateAndUpdate() }
    }

    private fun setupAnimations() {
        profileLayout.alpha = 0f
        infoCard.translationY = 800f
        nameInitial.scaleX = 0f
        nameInitial.scaleY = 0f
        updateBtn.alpha = 0f

        profileLayout.animate().alpha(1f).setDuration(800).start()
        infoCard.animate().translationY(0f).setDuration(1200).setInterpolator(AnticipateOvershootInterpolator(1.0f)).start()
        nameInitial.animate().scaleX(1f).scaleY(1f).setDuration(1000).setStartDelay(400).setInterpolator(OvershootInterpolator()).start()
        updateBtn.animate().alpha(1f).setDuration(600).setStartDelay(1000).start()
    }

    private fun setupSpinners() {
        val deptListWithDefault = listOf("Select Department") + departments
        val deptAdapter = ArrayAdapter(this, R.layout.spinner_item, deptListWithDefault)
        deptAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        departmentSpinner.adapter = deptAdapter

        val yearAdapter = ArrayAdapter(this, R.layout.spinner_item, years)
        yearAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        admissionYearSpinner.adapter = yearAdapter
    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            originalName = doc.getString("name") ?: ""
            userRegNo = doc.getString("regNo") ?: "XXXX-AA-XX"
            originalDept = doc.getString("department") ?: ""
            originalYear = doc.getString("admissionYear") ?: ""
            originalPhone = doc.getString("phone") ?: ""

            nameField.setText(originalName)
            phoneField.setText(originalPhone) 
            headerName.text = originalName
            headerRegNo.text = userRegNo
            nameInitial.text = originalName.trim().firstOrNull()?.uppercase().toString().ifEmpty { "U" }

            setSpinner(departmentSpinner, originalDept)
            setSpinner(admissionYearSpinner, originalYear)
        }
    }

    private fun setSpinner(spinner: Spinner, value: String) {
        val adapter = spinner.adapter ?: return
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i).toString() == value) {
                spinner.setSelection(i)
                break
            }
        }
    }

    private fun validateAndUpdate() {
        val n = nameField.text.toString().trim()
        val p = phoneField.text.toString().trim()
        val d = departmentSpinner.selectedItem.toString()
        val y = admissionYearSpinner.selectedItem.toString()

        if (n == originalName && p == originalPhone && d == originalDept && y == originalYear) {
            Toast.makeText(this, "No changes detected", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (!ValidationUtils.isValidName(n)) {
            nameField.error = "Invalid name"; return
        }
        if (!ValidationUtils.isValidPhone(p)) {
            phoneField.error = "Invalid phone"; return
        }

        updateBtn.isEnabled = false
        saveChanges(n, d, y, p)
    }

    private fun saveChanges(n: String, d: String, y: String, p: String) {
        val uid = auth.currentUser?.uid ?: return
        val updates = mapOf(
            "name" to n,
            "department" to d,
            "admissionYear" to y,
            "phone" to p
        )

        db.collection("users").document(uid).update(updates).addOnSuccessListener {
            Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, DashBoardActivity::class.java))
            finish()
        }.addOnFailureListener {
            updateBtn.isEnabled = true
            Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
