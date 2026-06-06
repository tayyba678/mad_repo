package com.example.unimatch

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList

class InterestAdapter(
    private var list: ArrayList<Interest>,
    private val onSelectionChanged: (Int) -> Unit
) : RecyclerView.Adapter<InterestAdapter.ViewHolder>() {

    private var filteredList: ArrayList<Interest> = ArrayList(list)

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
        val item = filteredList[position]
        holder.title.text = item.name
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = item.isSelected
        updateUI(holder, item, holder.itemView.context)

        holder.card.setOnClickListener {
            item.isSelected = !item.isSelected
            holder.checkBox.isChecked = item.isSelected
            updateUI(holder, item, holder.itemView.context)
            onSelectionChanged(list.count { it.isSelected })
        }

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            item.isSelected = isChecked
            updateUI(holder, item, holder.itemView.context)
            onSelectionChanged(list.count { it.isSelected })
        }
    }

    private fun updateUI(holder: ViewHolder, item: Interest, context: Context) {
        if (item.isSelected) {
            holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.mint))
            holder.title.setTextColor(ContextCompat.getColor(context, R.color.primary))
        } else {
            holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
            holder.title.setTextColor(ContextCompat.getColor(context, R.color.black))
        }
    }

    override fun getItemCount(): Int = filteredList.size

    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            ArrayList(list)
        } else {
            val resultList = ArrayList<Interest>()
            for (item in list) {
                if (item.name.lowercase(Locale.ROOT).contains(query.lowercase(Locale.ROOT))) {
                    resultList.add(item)
                }
            }
            resultList
        }
        notifyDataSetChanged()
    }
}
