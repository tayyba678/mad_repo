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

        val currentUid = auth.currentUser?.uid ?: return

        db.collection("users").document(currentUid).get()
            .addOnSuccessListener { currentDoc ->

                val myInterests = currentDoc.get("interests") as? List<String> ?: emptyList()

                db.collection("users").get()
                    .addOnSuccessListener { result ->

                        var bestUid: String? = null
                        var bestScore = 0

                        for (doc in result.documents) {

                            val uid = doc.id
                            if (uid == currentUid) continue

                            val otherInterests = doc.get("interests") as? List<String> ?: emptyList()

                            val common = myInterests.intersect(otherInterests.toSet()).size

                            if (common > bestScore) {
                                bestScore = common
                                bestUid = uid
                            }
                        }

                        // ⏳ FAKE LOADING DELAY (3 sec)
                        Handler(Looper.getMainLooper()).postDelayed({

                            if (bestUid == null) {
                                Toast.makeText(this, "No match found", Toast.LENGTH_SHORT).show()
                            } else {

                                Toast.makeText(
                                    this,
                                    "Match Found!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // 👉 MOVE TO CHAT
                                val intent = Intent(this@Match, ActivityChat::class.java)
                                intent.putExtra("matchUid", bestUid)
                                startActivity(intent)
                                finish()
                            }

                        }, 3000)
                    }
            }
    }
}