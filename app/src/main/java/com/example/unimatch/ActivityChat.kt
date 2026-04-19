package com.example.unimatch

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivityChat : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var btnLeave: TextView
    private lateinit var tvUserName: TextView

    private val messages = mutableListOf<Message>()
    private lateinit var adapter: ChatAdapter

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var matchUid: String? = null
    private var chatId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achat)

        recyclerView = findViewById(R.id.recyclerChat)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        btnLeave = findViewById(R.id.btnLeave)
        tvUserName = findViewById(R.id.tvUserName)

        matchUid = intent.getStringExtra("matchUid")

        val myUid = auth.currentUser?.uid
        val otherUid = matchUid

        if (myUid == null || otherUid == null) {
            Toast.makeText(this, "No match found. Try again!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, InterestActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }

        chatId = if (myUid < otherUid) {
            "${myUid}_${otherUid}"
        } else {
            "${otherUid}_${myUid}"
        }

        adapter = ChatAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        listenMessages()
        loadOtherUser()

        btnSend.setOnClickListener {
            sendMessage()
        }

        // ✅ Confirmation dialog before leaving
        btnLeave.setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("Leave Chat")
                .setMessage("Are you sure you want to leave this chat?")
                .setPositiveButton("Leave") { _, _ ->
                    startActivity(Intent(this, InterestActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                }
                .setNegativeButton("Stay", null)
                .show()
        }
    }

    private fun loadOtherUser() {
        val otherUid = matchUid ?: return

        db.collection("users").document(otherUid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("name") ?: "User"
                    tvUserName.text = name

                    tvUserName.setOnClickListener {
                        val intent = Intent(this, UserProfile::class.java)
                        intent.putExtra("uid", otherUid)
                        startActivity(intent)
                    }
                }
            }
            .addOnFailureListener {
                tvUserName.text = "User"
            }
    }

    private fun sendMessage() {
        val text = etMessage.text.toString().trim()
        val uid = auth.currentUser?.uid ?: return

        if (text.isEmpty()) return

        val msg = Message(
            senderId = uid,
            message = text,
            timestamp = System.currentTimeMillis()
        )

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(msg)
            .addOnFailureListener {
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
            }

        etMessage.setText("")
    }

    private fun listenMessages() {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { value, error ->

                if (error != null) {
                    Toast.makeText(this, "Failed to load messages", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                messages.clear()

                value?.forEach {
                    val msg = it.toObject(Message::class.java)
                    messages.add(msg)
                }

                adapter.notifyDataSetChanged()

                if (messages.isNotEmpty()) {
                    recyclerView.scrollToPosition(messages.size - 1)
                }
            }
    }
}