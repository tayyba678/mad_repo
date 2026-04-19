package com.example.unimatch

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import android.widget.LinearLayout

class InterestActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnContinue: Button
    private lateinit var adapter: InterestAdapter
    private lateinit var selectedCount: TextView  // ✅

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var menuBtn: TextView
    private lateinit var btnProfile: LinearLayout
    private lateinit var btnLogout: LinearLayout

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val interests = arrayListOf(

        // CS / SOFTWARE
        Interest("Programming"),
        Interest("Data Structures & Algorithms"),
        Interest("OOP"),
        Interest("Software Engineering"),
        Interest("Artificial Intelligence"),
        Interest("Data Science"),
        Interest("Web Development"),
        Interest("Mobile App Development"),
        Interest("Cloud Computing"),
        Interest("DevOps"),
        Interest("Operating Systems"),
        Interest("Cyber Security"),
        Interest("Computer Networks"),
        Interest("Database Systems"),
        Interest("UI/UX Design"),
        Interest("Game Development"),

        // ELECTRICAL / ELECTRONICS
        Interest("Circuit Design"),
        Interest("Embedded Systems"),
        Interest("IoT"),
        Interest("Robotics"),
        Interest("Power & Control Systems"),

        // MECHANICAL / CIVIL / CHEMICAL (Merged)
        Interest("CAD & 3D Design"),
        Interest("Thermodynamics & Fluid Mechanics"),
        Interest("Manufacturing"),
        Interest("Automotive Engineering"),
        Interest("Renewable Energy"),
        Interest("Structural & Construction"),
        Interest("Environmental Engineering"),
        Interest("Material Science"),

        // GENERAL / EXTRA
        Interest("Gaming"),
        Interest("Entrepreneurship"),
        Interest("Research"),
        Interest("Public Speaking"),
        Interest("Graphic Design"),
        Interest("Photography & Video Editing"),
        Interest("Content Creation"),
        Interest("Music"),
        Interest("Sports"),
        Interest("Fitness"),
        Interest("Reading"),
        Interest("Debating"),
        Interest("Community Service"),
        Interest("Event Management"),
        Interest("Teaching"),
        Interest("Finance & Investment"),
        Interest("Freelancing")

    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interest)

        recyclerView = findViewById(R.id.recyclerView)
        btnContinue = findViewById(R.id.btnContinue)
        drawerLayout = findViewById(R.id.drawerLayout)
        menuBtn = findViewById(R.id.menuBtn)
        btnProfile = findViewById(R.id.btnProfile)
        btnLogout = findViewById(R.id.btnLogout)
        selectedCount = findViewById(R.id.selectedCount)  // ✅

        // ✅ Pass callback to update counter
        adapter = InterestAdapter(interests) { count ->
            selectedCount.text = "$count selected"
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        menuBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        btnProfile.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, Profilee::class.java))
        }

        btnLogout.setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out") { _, _ ->
                    auth.signOut()
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        btnContinue.setOnClickListener {
            handleContinue()
        }
    }

    private fun handleContinue() {
        val selected = interests.filter { it.isSelected }

        if (selected.isEmpty()) {
            Toast.makeText(this, "Please select at least one interest", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginScreen::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }

        btnContinue.isEnabled = false

        val data = hashMapOf(
            "interests" to selected.map { it.name },
            "updatedAt" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Interests saved!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, Match::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
            .addOnFailureListener {
                btnContinue.isEnabled = true
                Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}