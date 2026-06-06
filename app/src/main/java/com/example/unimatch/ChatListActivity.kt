package com.example.unimatch

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.LayoutAnimationController
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatListActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ChatListAdapter
    private lateinit var noChatText: View
    private lateinit var header: View

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val list = mutableListOf<ChatUser>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        recycler = findViewById(R.id.chatRecycler)
        noChatText = findViewById(R.id.noChatText)
        header = findViewById(R.id.header)
        
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = ChatListAdapter(list) { user ->
            val intent = Intent(this, ActivityChat::class.java).apply {
                putExtra("matchUid", user.uid)
                putExtra("name", user.name)
            }
            startActivity(intent)
        }
        recycler.adapter = adapter

        setupAnimations()
        loadConversations()
    }

    private fun setupAnimations() {
        header.translationY = -200f
        header.animate().translationY(0f).setDuration(600).start()
        
        // Simple scale and fade for the recycler view
        recycler.alpha = 0f
        recycler.animate().alpha(1f).setDuration(1000).setStartDelay(300).start()
    }

    private fun loadConversations() {
        val currentUid = auth.currentUser?.uid ?: return

        db.collection("chats")
            .whereArrayContains("users", currentUid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener

                list.clear()
                if (snapshots.isEmpty) {
                    noChatText.visibility = View.VISIBLE
                    recycler.visibility = View.GONE
                    return@addSnapshotListener
                }

                noChatText.visibility = View.GONE
                recycler.visibility = View.VISIBLE

                for (doc in snapshots.documents) {
                    val users = doc.get("users") as? List<String> ?: continue
                    val lastMsg = doc.getString("lastMessage") ?: ""
                    val otherUid = users.firstOrNull { it != currentUid } ?: continue

                    db.collection("users").document(otherUid).get().addOnSuccessListener { userDoc ->
                        val name = userDoc.getString("name") ?: "User"
                        val profileImage = userDoc.getString("profileImage") ?: ""
                        
                        val existing = list.find { it.uid == otherUid }
                        if (existing == null) {
                            list.add(ChatUser(otherUid, name, lastMsg, profileImage))
                            list.sortByDescending { doc.getLong("timestamp") }
                            adapter.notifyDataSetChanged()
                        } else {
                            val index = list.indexOf(existing)
                            list[index] = ChatUser(otherUid, name, lastMsg, profileImage)
                            adapter.notifyItemChanged(index)
                        }
                    }
                }
            }
    }
}
