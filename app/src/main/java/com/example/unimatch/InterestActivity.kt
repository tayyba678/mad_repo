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

class InterestActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnContinue: Button
    private lateinit var adapter: InterestAdapter

    // 🔥 DRAWER
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var menuBtn: TextView
    private lateinit var btnProfile: TextView
    private lateinit var btnLogout: TextView

    // 🔥 FIREBASE
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val interests = arrayListOf(
        Interest("Programming"),
        Interest("Data Structures"),
        Interest("Algorithms"),
        Interest("OOP"),
        Interest("Software Engineering"),
        Interest("Artificial Intelligence"),
        Interest("Machine Learning"),
        Interest("Deep Learning"),
        Interest("Data Science"),
        Interest("Big Data"),
        Interest("Data Analysis"),
        Interest("Gaming"),
        Interest("Esports"),
        Interest("Mobile Gaming"),
        Interest("PC Gaming"),
        Interest("Game Development"),
        Interest("Game Design"),
        Interest("Web Development"),
        Interest("Frontend Development"),
        Interest("Backend Development"),
        Interest("Full Stack Development"),
        Interest("Mobile App Development"),
        Interest("Cloud Computing"),
        Interest("DevOps"),
        Interest("Operating Systems"),
        Interest("Cyber Security"),
        Interest("Ethical Hacking"),
        Interest("Computer Networks"),
        Interest("Database Systems"),
        Interest("UI/UX Design"),
        Interest("Entrepreneurship"),
        Interest("Product Management"),
        Interest("Research"),
        Interest("Public Speaking")
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

        adapter = InterestAdapter(interests)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // ☰ OPEN DRAWER
        menuBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // 👤 PROFILE
        btnProfile.setOnClickListener {
            startActivity(Intent(this, Profilee::class.java))
        }

        // 🚪 LOGOUT
        btnLogout.setOnClickListener {

            auth.signOut()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            finish()
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

        val uid = auth.currentUser?.uid

        if (uid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedList = selected.map { it.name }

        // 🔥 SAVE ONLY INTERESTS (MERGE WITH EXISTING USER DATA)


        val data = hashMapOf(
            "interests" to selectedList,
            "updatedAt" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(uid)
            .set(data, SetOptions.merge())   // ⭐ IMPORTANT LINE
            .addOnSuccessListener {

                Toast.makeText(this, "Interests saved successfully", Toast.LENGTH_SHORT).show()

                // 👉 NEXT SCREEN (optional)
                // startActivity(Intent(this, MatchActivity::class.java))

            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }
}