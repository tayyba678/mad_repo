package com.example.unimatch

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatListActivity : BaseActivity() {

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
        recycler.alpha = 0f
        recycler.animate().alpha(1f).setDuration(1000).setStartDelay(300).start()
    }

    private fun loadConversations() {
        val currentUid = auth.currentUser?.uid ?: return

        db.collection("chats")
            .whereArrayContains("users", currentUid)
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener

                if (snapshots.isEmpty) {
                    noChatText.visibility = View.VISIBLE
                    recycler.visibility = View.GONE
                    list.clear()
                    adapter.notifyDataSetChanged()
                    return@addSnapshotListener
                }

                noChatText.visibility = View.GONE
                recycler.visibility = View.VISIBLE

                snapshots.documents.forEach { doc ->
                    val users = doc.get("users") as? List<String> ?: return@forEach
                    val lastMsg = doc.getString("lastMessage") ?: ""
                    val timestamp = doc.getLong("timestamp") ?: 0L
                    val otherUid = users.firstOrNull { it != currentUid } ?: return@forEach

                    db.collection("users").document(otherUid).get().addOnSuccessListener { userDoc ->
                        val name = userDoc.getString("name") ?: "User"
                        val dept = userDoc.getString("department") ?: "No Dept"
                        val phone = userDoc.getString("phone") ?: "No Phone"
                        
                        // Pass all fields to ChatUser to fix constructor error and show info in history
                        val newUser = ChatUser(otherUid, name, lastMsg, timestamp, dept, phone)
                        
                        val existingIndex = list.indexOfFirst { it.uid == otherUid }
                        if (existingIndex != -1) {
                            list[existingIndex] = newUser
                        } else {
                            list.add(newUser)
                        }

                        // Always sort by newest message first
                        list.sortByDescending { it.timestamp }
                        adapter.notifyDataSetChanged()
                    }
                }
            }
    }
}
