package com.example.unimatch

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class Profilee : AppCompatActivity() {

    private lateinit var nameField: EditText
    private lateinit var regNoField: EditText
    private lateinit var departmentSpinner: Spinner
    private lateinit var admissionYearSpinner: Spinner
    private lateinit var phoneField: EditText
    private lateinit var updateBtn: Button

    private lateinit var headerName: TextView
    private lateinit var headerDept: TextView

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // ✅ Store original values to detect changes
    private var originalName = ""
    private var originalRegNo = ""
    private var originalDept = ""
    private var originalYear = ""
    private var originalPhone = ""

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
        setContentView(R.layout.activity_profilee)

        nameField = findViewById(R.id.name)
        regNoField = findViewById(R.id.regNo)
        departmentSpinner = findViewById(R.id.departmentSpinner)
        admissionYearSpinner = findViewById(R.id.admissionYearSpinner)
        phoneField = findViewById(R.id.phone)
        updateBtn = findViewById(R.id.updateBtn)
        headerName = findViewById(R.id.headerName)
        headerDept = findViewById(R.id.headerDept)

        setupSpinners()
        loadProfile()

        nameField.addTextChangedListener(SimpleTextWatcher {
            headerName.text = it.ifEmpty { "Your Name" }
        })

        updateBtn.setOnClickListener {
            updateProfile()
        }
    }

    private fun setupSpinners() {
        val deptAdapter = ArrayAdapter(this, R.layout.spinner_item, departments)
        deptAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        departmentSpinner.adapter = deptAdapter

        val yearAdapter = ArrayAdapter(this, R.layout.spinner_item, years)
        yearAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        admissionYearSpinner.adapter = yearAdapter
    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val n = doc.getString("name") ?: ""
                    val d = doc.getString("department") ?: ""

                    val y = when (val raw = doc.get("admissionYear")) {
                        is String -> raw
                        is Long -> raw.toString()
                        is Int -> raw.toString()
                        else -> ""
                    }

                    val r = doc.getString("regNo") ?: ""
                    val p = doc.getString("phone") ?: ""

                    nameField.setText(n)
                    regNoField.setText(r)
                    phoneField.setText(p)

                    headerName.text = n.ifEmpty { "Your Name" }
                    headerDept.text = d.ifEmpty { "Department" }

                    val deptIndex = departments.indexOf(d)
                    if (deptIndex >= 0) departmentSpinner.setSelection(deptIndex)

                    val yearIndex = years.indexOf(y)
                    if (yearIndex >= 0) admissionYearSpinner.setSelection(yearIndex)

                    // ✅ Save originals AFTER loading
                    originalName = n
                    originalRegNo = r
                    originalDept = d
                    originalYear = y
                    originalPhone = p
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfile() {
        val uid = auth.currentUser?.uid ?: return

        val n = nameField.text.toString().trim()
        val r = regNoField.text.toString().trim()
        val d = departmentSpinner.selectedItem.toString()
        val yString = admissionYearSpinner.selectedItem.toString()
        val p = phoneField.text.toString().trim()

        // Validations
        if (n.isEmpty()) { nameField.error = "Required"; return }
        if (r.isEmpty()) { regNoField.error = "Required"; return }
        if (d == "Select Department") {
            Toast.makeText(this, "Select department", Toast.LENGTH_SHORT).show()
            return
        }
        if (yString == "Select Year") {
            Toast.makeText(this, "Select year", Toast.LENGTH_SHORT).show()
            return
        }
        if (p.isEmpty()) { phoneField.error = "Required"; return }

        // ✅ Check if anything actually changed
        val hasChanges = n != originalName ||
                r != originalRegNo ||
                d != originalDept ||
                yString != originalYear ||
                p != originalPhone

        if (!hasChanges) {
            Toast.makeText(this, "Nothing to update", Toast.LENGTH_SHORT).show()
            // ✅ Navigate to InterestActivity even if nothing changed
            startActivity(Intent(this, InterestActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
            return
        }

        updateBtn.isEnabled = false

        val updates = hashMapOf<String, Any>(
            "name" to n,
            "regNo" to r,
            "department" to d,
            "admissionYear" to yString,
            "phone" to p,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        db.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener {
                updateBtn.isEnabled = true
                headerDept.text = d

                // ✅ Update originals after successful save
                originalName = n
                originalRegNo = r
                originalDept = d
                originalYear = yString
                originalPhone = p

                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()

                // ✅ Navigate to InterestActivity after toast
                startActivity(Intent(this, InterestActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                })
            }
            .addOnFailureListener {
                updateBtn.isEnabled = true
                Toast.makeText(this, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}