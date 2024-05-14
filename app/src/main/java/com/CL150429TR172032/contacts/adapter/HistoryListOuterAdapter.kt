package com.CL150429TR172032.contacts.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.CL150429TR172032.contacts.R
import com.CL150429TR172032.contacts.dto.History
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryListOuterAdapter (private val historyMap: Map<Date, List<History>>) : RecyclerView.Adapter<HistoryListOuterAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    private val dates: List<Date> = historyMap.keys.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_outer_row_history_list, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val date = dates[position]
        val historyItems = historyMap[date] ?: emptyList<History>()

        holder.dateTextView.text = formatDate(date)

        val innerAdapter = HistoryListInnerAdapter(historyItems)
        holder.innerRecyclerView.adapter = innerAdapter
    }

    override fun getItemCount(): Int {
        return dates.size
    }

    private fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val innerRecyclerView: RecyclerView = itemView.findViewById(R.id.innerRecyclerView)

        init {
            innerRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
        }
    }

}