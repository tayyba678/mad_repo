package com.example.unimatch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatAdapter(private val messages: List<Message>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val currentUid = FirebaseAuth.getInstance().currentUser?.uid
    private val db = FirebaseFirestore.getInstance()

    private val nameCache = mutableMapOf<String, String>()

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutReceived: LinearLayout = itemView.findViewById(R.id.layoutReceived)
        val layoutSent: LinearLayout = itemView.findViewById(R.id.layoutSent)
        val msgTextLeft: TextView = itemView.findViewById(R.id.msgTextLeft)
        val msgTextRight: TextView = itemView.findViewById(R.id.msgTextRight)
        val tvSenderName: TextView = itemView.findViewById(R.id.tvSenderName)
        val tvInitialLeft: TextView = itemView.findViewById(R.id.tvInitialLeft)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)

        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val msg = messages[position]
        val isMine = msg.senderId == currentUid

        if (isMine) {
            holder.layoutSent.visibility = View.VISIBLE
            holder.layoutReceived.visibility = View.GONE
            holder.msgTextRight.text = msg.message
        } else {
            holder.layoutReceived.visibility = View.VISIBLE
            holder.layoutSent.visibility = View.GONE
            holder.msgTextLeft.text = msg.message

            val senderId = msg.senderId
            if (nameCache.containsKey(senderId)) {
                val name = nameCache[senderId] ?: "User"
                holder.tvSenderName.text = name
                holder.tvInitialLeft.text = name.trim().firstOrNull()?.uppercase().toString().ifEmpty { "U" }
            } else {
                holder.tvSenderName.text = "..."
                holder.tvInitialLeft.text = "U"

                db.collection("users")
                    .document(senderId)
                    .get()
                    .addOnSuccessListener { doc ->
                        val name = doc.getString("name") ?: "User"
                        nameCache[senderId] = name
                        holder.tvSenderName.text = name
                        holder.tvInitialLeft.text = name.trim().firstOrNull()?.uppercase().toString().ifEmpty { "U" }
                    }
                    .addOnFailureListener {
                        holder.tvSenderName.text = "User"
                        holder.tvInitialLeft.text = "U"
                    }
            }
        }
    }

    override fun getItemCount(): Int = messages.size
}
