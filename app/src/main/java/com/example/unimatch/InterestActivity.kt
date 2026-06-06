package com.example.unimatch

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.*

class InterestActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnContinue: Button
    private lateinit var adapter: InterestAdapter
    private lateinit var selectedCount: TextView
    private lateinit var headerCard: View
    private lateinit var searchInterests: EditText
    private lateinit var searchCard: View

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val interests = arrayListOf(
        Interest("Android Development"), Interest("Artificial Intelligence"), Interest("Backend Development"),
        Interest("Blockchain"), Interest("Cloud Computing"), Interest("Cyber Security"),
        Interest("Data Science"), Interest("Database Management"), Interest("DevOps"),
        Interest("Digital Marketing"), Interest("Embedded Systems"), Interest("Ethical Hacking"),
        Interest("Frontend Development"), Interest("Game Development"), Interest("Graphic Design"),
        Interest("Information Security"), Interest("Internet of Things (IoT)"), Interest("Java Programming"),
        Interest("Kotlin"), Interest("Machine Learning"), Interest("Mobile App Development"),
        Interest("Network Administration"), Interest("Object Oriented Programming"), Interest("Python Programming"),
        Interest("Quality Assurance"), Interest("React Native"), Interest("Software Engineering"),
        Interest("UI/UX Design"), Interest("Web Development"), Interest("Cricket"),
        Interest("Football"), Interest("Table Tennis"), Interest("Badminton"),
        Interest("Gaming"), Interest("Photography"), Interest("Public Speaking"),
        Interest("Content Writing"), Interest("Freelancing"), Interest("Video Editing"),
        Interest("Chess"), Interest("Music"), Interest("Reading"), Interest("Cooking")
    ).apply { sortBy { it.name } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interest)

        recyclerView = findViewById(R.id.recyclerView)
        btnContinue = findViewById(R.id.btnContinue)
        selectedCount = findViewById(R.id.selectedCount)
        headerCard = findViewById(R.id.headerCard)
        searchInterests = findViewById(R.id.searchInterests)
        searchCard = searchInterests.parent.parent as View

        adapter = InterestAdapter(interests) { count ->
            selectedCount.text = "$count selected"
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        setupAnimations()

        searchInterests.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnContinue.setOnClickListener { handleContinue() }
    }

    private fun setupAnimations() {
        headerCard.translationY = -300f
        searchCard.translationY = 400f
        searchCard.alpha = 0f
        recyclerView.translationY = 600f
        recyclerView.alpha = 0f
        btnContinue.translationY = 200f
        btnContinue.alpha = 0f

        headerCard.animate().translationY(0f).setDuration(800).setInterpolator(OvershootInterpolator()).start()
        
        searchCard.animate().translationY(0f).alpha(1f).setDuration(800).setStartDelay(300).setInterpolator(OvershootInterpolator()).start()
        
        recyclerView.animate().translationY(0f).alpha(1f).setDuration(800).setStartDelay(500).setInterpolator(DecelerateInterpolator()).start()
        
        btnContinue.animate().translationY(0f).alpha(1f).setDuration(600).setStartDelay(900).setInterpolator(AnticipateOvershootInterpolator()).start()
    }

    private fun handleContinue() {
        val selected = interests.filter { it.isSelected }
        if (selected.isEmpty()) {
            Toast.makeText(this, "Please select at least one interest", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = auth.currentUser?.uid ?: return
        val data = hashMapOf(
            "interests" to selected.map { it.name },
            "updatedAt" to System.currentTimeMillis()
        )

        db.collection("users").document(uid).set(data, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Interests saved!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Match::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
    }
}
