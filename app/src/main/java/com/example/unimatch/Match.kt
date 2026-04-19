package com.example.unimatch

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Match : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match)

        findMatch()
    }

    private fun findMatch() {
        val currentUid = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginScreen::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }

        db.collection("users").document(currentUid).get()
            .addOnSuccessListener { currentDoc ->
                val myInterests = currentDoc.get("interests") as? List<String> ?: emptyList()

                db.collection("users").get()
                    .addOnSuccessListener { result ->

                        data class Candidate(val uid: String, val score: Int)

                        val candidates = mutableListOf<Candidate>()

                        for (doc in result.documents) {
                            val uid = doc.id
                            if (uid == currentUid) continue

                            val otherInterests = doc.get("interests") as? List<String> ?: emptyList()
                            val score = myInterests.intersect(otherInterests.toSet()).size

                            if (score > 0) {
                                candidates.add(Candidate(uid, score))
                            }
                        }

                        // ✅ Pick best match by highest score
                        val bestMatch = candidates.sortedByDescending { it.score }.firstOrNull()

                        Handler(Looper.getMainLooper()).postDelayed({

                            if (bestMatch == null) {
                                // ✅ No match found → back to InterestActivity
                                Toast.makeText(
                                    this,
                                    "No match found. Try updating your interests!",
                                    Toast.LENGTH_LONG
                                ).show()
                                startActivity(Intent(this, InterestActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                })
                                finish()

                            } else {
                                Toast.makeText(this, "Match Found!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, ActivityChat::class.java).apply {
                                    putExtra("matchUid", bestMatch.uid)
                                })
                                finish()
                            }

                        }, 3000)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Matching failed: ${it.message}", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, InterestActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                        finish()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Could not load your profile: ${it.message}", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, InterestActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
    }
}