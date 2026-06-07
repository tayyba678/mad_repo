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

class User_registration : BaseActivity() {

    private lateinit var name: EditText
    private lateinit var regNo: EditText
    private lateinit var departmentSearch: AutoCompleteTextView
    private lateinit var admissionYearSpinner: Spinner
    private lateinit var phone: EditText
    private lateinit var genderGroup: RadioGroup
    private lateinit var submitBtn: Button
    private lateinit var nameInitial: TextView
    private lateinit var mainLayout: View
    private lateinit var registrationCard: View

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val departments = listOf(
        "Architecture", "Artificial Intelligence", "Biology",
        "Business Administration", "Chemical Engineering", "Chemistry",
        "Civil Engineering", "Computer Science", "Data Science", "Economics",
        "Electrical Engineering", "Electronics Engineering", "Engineering Management",
        "Environmental Engineering", "Fine Arts", "Geological Engineering",
        "Industrial Engineering", "Information Technology", "Mathematics",
        "Mechanical Engineering", "Mechatronics Engineering", "Metallurgical Engineering",
        "Mining Engineering", "Petroleum Engineering", "Physics", "Polymer Engineering",
        "Software Engineering", "Textile Engineering", "Other"
    ).sorted()

    private val years = listOf("Select Year") + (1980..2030).map { it.toString() }.reversed()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_registration)

        name = findViewById(R.id.name)
        regNo = findViewById(R.id.regNo)
        departmentSearch = findViewById(R.id.departmentSearch)
        admissionYearSpinner = findViewById(R.id.admissionYearSpinner)
        phone = findViewById(R.id.phone)
        genderGroup = findViewById(R.id.genderGroup)
        submitBtn = findViewById(R.id.submitBtn)
        nameInitial = findViewById(R.id.nameInitial)
        mainLayout = findViewById(R.id.mainLayout)
        registrationCard = findViewById(R.id.registrationCard)

        setupDepartmentSearch()
        setupYearSpinner()
        setupAnimations()

        name.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val firstChar = s?.toString()?.trim()?.firstOrNull()?.uppercase() ?: "U"
                nameInitial.text = firstChar
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        submitBtn.setOnClickListener { validateAndSave() }
    }

    private fun setupAnimations() {
        mainLayout.alpha = 0f
        registrationCard.translationY = 1200f
        nameInitial.scaleX = 0f
        nameInitial.scaleY = 0f
        submitBtn.alpha = 0f
        submitBtn.translationY = 200f

        mainLayout.animate().alpha(1f).setDuration(600).start()

        registrationCard.animate()
            .translationY(0f)
            .setDuration(1100)
            .setInterpolator(AnticipateOvershootInterpolator(1.2f))
            .setStartDelay(200)
            .start()

        nameInitial.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(800)
            .setStartDelay(900)
            .setInterpolator(OvershootInterpolator(2f))
            .start()

        submitBtn.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(700)
            .setStartDelay(1300)
            .start()
    }

    private fun setupDepartmentSearch() {
        val adapter = ArrayAdapter(this, R.layout.spinner_item, departments)
        departmentSearch.setAdapter(adapter)
        departmentSearch.setDropDownBackgroundResource(android.R.color.white)
    }

    private fun setupYearSpinner() {
        val adapter = ArrayAdapter(this, R.layout.spinner_item, years)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        admissionYearSpinner.adapter = adapter
        admissionYearSpinner.setPopupBackgroundResource(android.R.color.white)
    }

    private fun validateAndSave() {
        val n = name.text.toString().trim()
        val r = regNo.text.toString().trim().lowercase()
        val p = phone.text.toString().trim()
        val d = departmentSearch.text.toString().trim()
        val y = admissionYearSpinner.selectedItem.toString()
        val selectedGenderId = genderGroup.checkedRadioButtonId

        if (!ValidationUtils.isValidName(n)) {
            name.error = "Name should only contain letters"; return
        }
        if (!ValidationUtils.isValidRegNo(r)) {
            regNo.error = "Format: XXXX-AA-XX"; return
        }
        if (!ValidationUtils.isValidPhone(p)) {
            phone.error = "Use 03XXXXXXXXX or +92XXXXXXXXXX"; return
        }
        if (d.isEmpty()) {
            departmentSearch.error = "Please enter or select department"; return
        }
        if (y == "Select Year" || selectedGenderId == -1) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        submitBtn.isEnabled = false
        db.collection("users").whereEqualTo("regNo", r).get().addOnSuccessListener { docs ->
            if (!docs.isEmpty) {
                regNo.error = "This registration number is already in use"
                submitBtn.isEnabled = true
            } else {
                saveUserToFirestore(n, r, d, y, p, findViewById<RadioButton>(selectedGenderId).text.toString())
            }
        }.addOnFailureListener { submitBtn.isEnabled = true }
    }

    private fun saveUserToFirestore(n: String, r: String, d: String, y: String, p: String, gender: String) {
        val uid = auth.currentUser?.uid ?: return
        val userMap = mutableMapOf(
            "uid" to uid, "name" to n, "regNo" to r, "department" to d,
            "admissionYear" to y, "phone" to p, "gender" to gender
        )

        db.collection("users").document(uid).set(userMap).addOnSuccessListener {
            Toast.makeText(this, "Profile Saved! Welcome.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, DashBoardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }.addOnFailureListener {
            submitBtn.isEnabled = true
            Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
