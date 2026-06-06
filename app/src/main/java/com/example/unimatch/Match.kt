package com.example.unimatch

import android.content.Intent
import android.os.Bundle
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

        findBestMatch()
    }

    private fun findBestMatch() {
        val currentUid = auth.currentUser?.uid ?: return

        db.collection("users").document(currentUid).get()
            .addOnSuccessListener { currentDoc ->
                val myInterests = currentDoc.get("interests") as? List<String> ?: emptyList()

                if (myInterests.isEmpty()) {
                    goBackToInterests("Please select interests first")
                    return@addOnSuccessListener
                }

                db.collection("users").get()
                    .addOnSuccessListener { result ->
                        val candidates = mutableListOf<Pair<String, Int>>()

                        for (doc in result.documents) {
                            val uid = doc.id
                            if (uid == currentUid) continue

                            val otherInterests = doc.get("interests") as? List<String> ?: continue
                            val score = myInterests.intersect(otherInterests.toSet()).size

                            if (score > 0) {
                                candidates.add(uid to score)
                            }
                        }

                        val bestMatch = candidates.maxByOrNull { it.second }

                        if (bestMatch == null) {
                            Toast.makeText(this, "No match found", Toast.LENGTH_LONG).show()
                            goBackToInterests()
                            return@addOnSuccessListener
                        }

                        // FIXED: Navigate to ActivityChat instead of ChatListActivity
                        startActivity(
                            Intent(this, ActivityChat::class.java).apply {
                                putExtra("matchUid", bestMatch.first)
                            }
                        )
                        finish()
                    }
            }
    }

    private fun goBackToInterests(msg: String? = null) {
        msg?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        startActivity(Intent(this, InterestActivity::class.java))
        finish()
    }
}
