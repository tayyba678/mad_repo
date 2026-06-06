package com.example.unimatch

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashBoardActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var interestCard: CardView
    private lateinit var chatCard: CardView
    private lateinit var triangleCard: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)

        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.navigationView)
        interestCard = findViewById(R.id.interestCard)
        chatCard = findViewById(R.id.chatCard)
        triangleCard = findViewById(R.id.triangleCard)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open,
            R.string.close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        setupUserInfo()
        setupClickListeners()
        setupAnimations()
    }

    private fun setupUserInfo() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener {
                val name = it.getString("name") ?: "User"
                findViewById<TextView>(R.id.welcomeText).text = "Welcome $name"
                
                val headerView = navView.getHeaderView(0)
                val navName = headerView.findViewById<TextView>(R.id.navName)
                val navInitial = headerView.findViewById<TextView>(R.id.navInitial)
                
                navName.text = name
                navInitial.text = name.trim().firstOrNull()?.uppercase().toString().ifEmpty { "U" }
            }
    }

    private fun setupClickListeners() {
        interestCard.setOnClickListener {
            startActivity(Intent(this, InterestActivity::class.java))
        }

        chatCard.setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
        }

        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_profile_update -> startActivity(Intent(this, Profilee::class.java))
                R.id.nav_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, LoginScreen::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun setupAnimations() {
        // Initial states
        triangleCard.translationY = -400f
        triangleCard.alpha = 0f
        interestCard.translationX = -800f
        chatCard.translationX = 800f

        // Execute animations
        triangleCard.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(1000)
            .setInterpolator(OvershootInterpolator())
            .start()

        interestCard.animate()
            .translationX(0f)
            .setDuration(1000)
            .setStartDelay(300)
            .setInterpolator(AnticipateOvershootInterpolator())
            .start()

        chatCard.animate()
            .translationX(0f)
            .setDuration(1000)
            .setStartDelay(500)
            .setInterpolator(AnticipateOvershootInterpolator())
            .start()
    }
}
