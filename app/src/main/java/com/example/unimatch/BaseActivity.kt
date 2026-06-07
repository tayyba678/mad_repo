package com.example.unimatch

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

open class BaseActivity : AppCompatActivity() {

    private var notificationListener: ListenerRegistration? = null
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        startGlobalNotificationListener()
    }

    override fun onStop() {
        super.onStop()
        notificationListener?.remove()
    }

    // Override this in ActivityChat to return the current chatId to avoid notifying while in chat
    open fun getActiveChatId(): String? = null

    private fun startGlobalNotificationListener() {
        val uid = auth.currentUser?.uid ?: return
        
        notificationListener?.remove()

        notificationListener = db.collection("chats")
            .whereArrayContains("users", uid)
            .addSnapshotListener { snapshots, _ ->
                snapshots?.documentChanges?.forEach { change ->
                    if (change.type == DocumentChange.Type.MODIFIED) {
                        val doc = change.document
                        val lastSenderId = doc.getString("lastSenderId") ?: ""
                        
                        // Notify only if:
                        // 1. Someone else sent the message
                        // 2. We are NOT currently in the chat with this person
                        if (lastSenderId.isNotEmpty() && lastSenderId != uid && doc.id != getActiveChatId()) {
                            val lastMsg = doc.getString("lastMessage") ?: ""
                            
                            db.collection("users").document(lastSenderId).get().addOnSuccessListener { 
                                val name = it.getString("name") ?: "UniMatch"
                                sendNotification(name, lastMsg)
                            }
                        }
                    }
                }
            }
    }

    private fun sendNotification(title: String, body: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "global_chat_notif"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "UniMatch Messages", NotificationManager.IMPORTANCE_HIGH)
            nm.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        nm.notify(System.currentTimeMillis().toInt(), notification)
    }
}
