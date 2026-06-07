package com.example.unimatch

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ActivityChat : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: View
    private lateinit var btnLeave: TextView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserDetail: TextView
    private lateinit var tvInitial: TextView
    private lateinit var llUserHeader: View

    private val messages = mutableListOf<Message>()
    private lateinit var adapter: ChatAdapter

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var matchUid: String? = null
    private lateinit var chatId: String
    private var isDetailVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achat)

        recyclerView = findViewById(R.id.recyclerChat)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        btnLeave = findViewById(R.id.btnLeave)
        tvUserName = findViewById(R.id.tvUserName)
        tvUserDetail = findViewById(R.id.tvUserDetail)
        tvInitial = findViewById(R.id.tvInitial)
        llUserHeader = findViewById(R.id.llUserHeader)

        matchUid = intent.getStringExtra("matchUid")
        val myUid = auth.currentUser?.uid

        if (myUid.isNullOrEmpty() || matchUid.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid chat", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        chatId = listOf(myUid, matchUid!!).sorted().joinToString("_")

        adapter = ChatAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadOtherUser()
        listenMessages()

        btnSend.setOnClickListener { sendMessage() }
        
        // Navigate back to Dashboard
        btnLeave.setOnClickListener { navigateToDashboard() }
        
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToDashboard()
            }
        })

        llUserHeader.setOnClickListener {
            isDetailVisible = !isDetailVisible
            tvUserDetail.visibility = if (isDetailVisible) View.VISIBLE else View.GONE
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashBoardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    override fun getActiveChatId(): String? = chatId

    private fun sendMessage() {
        val text = etMessage.text.toString().trim()
        val uid = auth.currentUser?.uid ?: return
        if (text.isEmpty()) return

        val timestamp = System.currentTimeMillis()
        val msg = Message(senderId = uid, message = text, timestamp = timestamp)

        db.collection("chats").document(chatId).collection("messages").add(msg)

        val chatMeta = mapOf(
            "lastMessage" to text,
            "timestamp" to timestamp,
            "lastSenderId" to uid,
            "users" to listOf(uid, matchUid)
        )
        db.collection("chats").document(chatId).set(chatMeta, SetOptions.merge())

        etMessage.setText("")
    }

    private fun listenMessages() {
        db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { value, _ ->
                messages.clear()
                value?.forEach { doc ->
                    val m = doc.toObject(Message::class.java)
                    if (m != null) messages.add(m)
                }
                adapter.notifyDataSetChanged()
                if (messages.isNotEmpty()) {
                    recyclerView.scrollToPosition(messages.size - 1)
                }
            }
    }

    private fun loadOtherUser() {
        val uid = matchUid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            val name = doc.getString("name") ?: "User"
            val dept = doc.getString("department") ?: "No Dept"
            val phone = doc.getString("phone") ?: "No Phone"

            tvUserName.text = name
            // CONFIDENTIALITY: Show Department instead of RegNo
            tvUserDetail.text = String.format("%s | %s", dept, phone)
            
            tvInitial.visibility = View.VISIBLE
            tvInitial.text = name.trim().firstOrNull()?.uppercase().toString().ifEmpty { "U" }
        }
    }
}
