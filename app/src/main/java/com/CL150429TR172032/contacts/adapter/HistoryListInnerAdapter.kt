package com.CL150429TR172032.contacts.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.CL150429TR172032.contacts.R
import com.CL150429TR172032.contacts.dto.History
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryListInnerAdapter(private val historyItems: List<History>) :
    RecyclerView.Adapter<HistoryListInnerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_inner_row_history_list, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val historyItem = historyItems[position]
        holder.historyItemTextView.text = historyItem.event
        holder.historyItemHour.text = getHourAndMinuteFromDate(historyItem.date)
        if(historyItem.type == "CALL") {
            holder.historyItemIcon.setImageResource(R.drawable.baseline_phone_24)
        }else {
            holder.historyItemIcon.setImageResource(R.drawable.baseline_send_24)
        }
    }

    override fun getItemCount(): Int {
        return historyItems.size
    }

    private fun getHourAndMinuteFromDate(date: Date): String {
        val dateFormat = SimpleDateFormat("HH:mm a", Locale.getDefault())
        return dateFormat.format(date)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val historyItemTextView: TextView = itemView.findViewById(R.id.historyItemTextView)
        val historyItemHour: TextView = itemView.findViewById(R.id.hourCard)
        val historyItemIcon: ImageButton = itemView.findViewById(R.id.phoneOrText);
    }
}