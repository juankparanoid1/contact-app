package com.CL150429TR172032.contacts.screens

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.CL150429TR172032.contacts.R
import com.CL150429TR172032.contacts.adapter.HistoryListOuterAdapter
import com.CL150429TR172032.contacts.dto.History
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.*

class ContactHistory : AppCompatActivity() {

    private lateinit var historyListRecyclerView: RecyclerView;
    private lateinit var firestore: FirebaseFirestore

    private lateinit var contactId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_history)

        contactId = intent.getStringExtra("contactId") ?: ""

        firestore = Firebase.firestore

        val allHistory = firestore.collection("history")
            .whereIn("contactId", listOf(contactId))
            .orderBy("date", Query.Direction.ASCENDING)

        historyListRecyclerView = findViewById(R.id.historyList)
        historyListRecyclerView.layoutManager = LinearLayoutManager(this)

        allHistory.addSnapshotListener { querySnapshot, _ ->
            if (querySnapshot != null && !querySnapshot.isEmpty) {
                val historyList = queryToHistoryList(querySnapshot)
                val historyMap = historyList.groupBy { formatDateWithoutTime(it.date) }
                Log.d("historyMap ", historyMap.toString())
                historyListRecyclerView.adapter = HistoryListOuterAdapter(historyMap)
            }
        }
    }

    fun queryToHistoryList(querySnapshot: QuerySnapshot?): List<History> {
        val historyList = ArrayList<History>()
        querySnapshot?.documents?.forEach { document ->
            val history = document.toObject(History::class.java)
            if (history != null) {
                historyList.add(history)
            }
        }
        return historyList
    }

    private fun formatDateWithoutTime(date: Date): Date {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = dateFormat.format(date)
        return dateFormat.parse(dateString)
    }
}