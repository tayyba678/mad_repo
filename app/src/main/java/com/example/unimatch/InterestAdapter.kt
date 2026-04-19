package com.example.unimatch

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class InterestAdapter(
    private val list: ArrayList<Interest>,
    private val onSelectionChanged: (Int) -> Unit  // ✅ callback
) : RecyclerView.Adapter<InterestAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.txtInterest)
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)
        val card: CardView = view as CardView
        val dot: View = view.findViewById(R.id.colorDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_interest_adapter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.title.text = item.name

        if (item.isSelected) {
            holder.card.setCardBackgroundColor(Color.parseColor("#F1F8E9"))
            holder.title.setTextColor(Color.parseColor("#2E7D32"))
            holder.title.paint.isFakeBoldText = true
            holder.dot.setBackgroundColor(Color.parseColor("#2E7D32"))
        } else {
            holder.card.setCardBackgroundColor(Color.WHITE)
            holder.title.setTextColor(Color.parseColor("#1A1A2E"))
            holder.title.paint.isFakeBoldText = false
            holder.dot.setBackgroundColor(Color.parseColor("#C8E6C9"))
        }

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = item.isSelected

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            item.isSelected = isChecked
            // ✅ notify count change
            onSelectionChanged(list.count { it.isSelected })
            holder.itemView.post { notifyItemChanged(position) }
        }

        holder.card.setOnClickListener {
            item.isSelected = !item.isSelected
            // ✅ notify count change
            onSelectionChanged(list.count { it.isSelected })
            holder.itemView.post { notifyItemChanged(position) }
        }
    }

    override fun getItemCount(): Int = list.size
}