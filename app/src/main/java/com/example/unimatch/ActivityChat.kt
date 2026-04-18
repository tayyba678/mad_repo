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

        matchUid = intent.getStringExtra("matchUid")

        val myUid = auth.currentUser?.uid ?: return

        // 🔥 CHAT ID
        chatId = if (myUid < matchUid!!) {
            myUid + "_" + matchUid
        } else {
            matchUid + "_" + myUid
        }

        adapter = ChatAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        listenMessages()

        btnSend.setOnClickListener {
            sendMessage()
        }

        // 🔴 LEAVE BUTTON
        btnLeave.setOnClickListener {

            val intent = Intent(this, InterestActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            finish()
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

        etMessage.setText("")
    }

    private fun listenMessages() {

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { value, _ ->

                messages.clear()

                value?.forEach {
                    val msg = it.toObject(Message::class.java)
                    messages.add(msg)
                }

                adapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(messages.size - 1)
            }
    }
}