package com.CL150429TR172032.contacts

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.CL150429TR172032.contacts.dto.History
import com.CL150429TR172032.contacts.screens.ContactForm
import com.CL150429TR172032.contacts.screens.ContactHistory
import com.CL150429TR172032.contacts.screens.ContactInfo
import com.CL150429TR172032.contacts.screens.ContactList

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, ContactList::class.java)
        startActivity(intent)
        finish()
    }
}