package com.CL150429TR172032.contacts.screens

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.CL150429TR172032.contacts.R
import com.CL150429TR172032.contacts.adapter.CarouselAdapter
import com.CL150429TR172032.contacts.adapter.ContactListAdapter
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore


class ContactList : AppCompatActivity() {

    private lateinit var carousel: RecyclerView;
    private lateinit var contactListRecyclerView: RecyclerView;

    private lateinit var textInputLayout: TextInputLayout
    private lateinit var searchInputText: TextInputEditText

    private lateinit var firestore: FirebaseFirestore
    private lateinit var carouselAdapter: CarouselAdapter
    private lateinit var allContactsAdapter: ContactListAdapter

    private lateinit var buttonAdd: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_list)

        firestore = Firebase.firestore
        val topContacts = firestore
            .collection("contacts")
            .whereEqualTo("favorite", true)
            .orderBy("name", Query.Direction.ASCENDING)
            .limit(50)

        carousel = findViewById(R.id.contactCarousel)
        carousel.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        carouselAdapter = CarouselAdapter(topContacts)
        carousel.adapter = carouselAdapter
        carouselAdapter.startListening()

        val allContacts = firestore
            .collection("contacts")
            .orderBy("name", Query.Direction.ASCENDING)
            .limit(50)
        allContactList(allContacts)

        searchInputText = findViewById<TextInputEditText>(R.id.searchInputText)
        searchInputText.addTextChangedListener(object : TextWatcher {
            private val handler = Handler(Looper.getMainLooper())
            private var searchRunnable: Runnable = Runnable {}
            private val debounceDuration = 500L // Adjust the debounce duration as needed

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                handler.removeCallbacks(searchRunnable)
                searchRunnable = Runnable {
                    val searchText = s.toString().lowercase().trim()
                    val searchContact = firestore
                        .collection("contacts")
                        .orderBy("name", Query.Direction.ASCENDING)
                        .whereEqualTo("nameLowerCase", searchText)
                        .limit(50)
                    val query: Query =
                        if (searchText.isNullOrBlank()) allContacts else searchContact
                    allContactList(query)
                }
                handler.postDelayed(searchRunnable, debounceDuration)
            }
        })

        buttonAdd = findViewById<ImageButton>(R.id.buttonAdd)
        buttonAdd.setOnClickListener {
            val newContactScreen = Intent(this, ContactForm::class.java)
            startActivity(newContactScreen)
        }
    }

    private fun allContactList(query: Query) {
        contactListRecyclerView = findViewById(R.id.contactList)
        contactListRecyclerView.layoutManager = LinearLayoutManager(this)
        allContactsAdapter = ContactListAdapter(query)
        contactListRecyclerView.adapter = allContactsAdapter
        allContactsAdapter.startListening()
    }
}