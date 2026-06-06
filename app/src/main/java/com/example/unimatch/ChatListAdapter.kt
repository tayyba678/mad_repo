package com.example.unimatch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatListAdapter(
    private val list: List<ChatUser>,
    private val onClick: (ChatUser) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name)
        val lastMsg: TextView = itemView.findViewById(R.id.lastMsg)
        val initial: TextView = itemView.findViewById(R.id.initial)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_history, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val user = list[position]

        holder.name.text = user.name
        holder.lastMsg.text = user.lastMessage

        // Display the first letter of the name
        holder.initial.text = user.name.trim().firstOrNull()?.uppercase().toString().ifEmpty { "U" }

        holder.itemView.setOnClickListener {
            onClick(user)
        }
    }

    override fun getItemCount(): Int = list.size
}
