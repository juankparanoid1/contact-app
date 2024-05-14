package com.CL150429TR172032.contacts.screens

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.get
import com.CL150429TR172032.contacts.R
import com.CL150429TR172032.contacts.dto.Contact
import com.CL150429TR172032.contacts.dto.EventType
import com.CL150429TR172032.contacts.dto.EventTypeShort
import com.CL150429TR172032.contacts.dto.History
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import java.util.Date


class ContactInfo : AppCompatActivity() {

    private lateinit var historyButton: Button
    private lateinit var contactId: String

    private lateinit var contactUserImage: ImageView
    private lateinit var nameLabelText: TextView
    private lateinit var cellphoneLabelText: TextView

    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var sendEmail: ImageButton
    private lateinit var whatsappCall: ImageButton
    private lateinit var whatsappMessage: ImageButton

    private lateinit var firestore: FirebaseFirestore

    val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_info)

        firestore = Firebase.firestore

        contactId = intent.getStringExtra("contactId") ?: ""

        Log.i("contactId ", contactId)

        val contactJson = intent.getStringExtra("contactInfo")
        val contact = gson.fromJson(contactJson, Contact::class.java)

        Log.i("contact gson ", contact.toString())

        if (contact != null) {
            contactUserImage = findViewById(R.id.contactUserImage)
            val image = if (contact.image.isNullOrEmpty()) {
                "https://firebasestorage.googleapis.com/v0/b/contacts-app-db1b7.appspot.com/o/images%2Fdefault-avatar.png?alt=media&token=a8306cf6-c542-40fc-80a2-f65944a5a29e"
            } else {
                contact.image
            }
            Picasso.get().load(image).into(contactUserImage)

            nameLabelText = findViewById(R.id.nameLabelText)
            nameLabelText.text = "${contact.name} ${contact.lastName}"

            cellphoneLabelText = findViewById(R.id.cellphoneLabelText)
            cellphoneLabelText.text = contact.cellphone
        }

        historyButton = findViewById(R.id.historyButton)

        historyButton.setOnClickListener {
            if (!contactId.isNullOrEmpty()) {
                val contactHistoryScreen = Intent(this, ContactHistory::class.java)
                contactHistoryScreen.putExtra("contactId", contactId)
                startActivity(contactHistoryScreen)
            }
        }

        sendEmail = findViewById(R.id.sendEmail)
        sendEmail.setOnClickListener {
            if (!contact.email.isNullOrEmpty()) {
                val emailIntent = Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:${contact.email}"))
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Hola ${contact.name}")
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Hola ${contact.name}");
                startActivity(emailIntent);
            }
        }

        whatsappCall = findViewById(R.id.whatsappCall)
        whatsappCall.setOnClickListener {
            val history = History(
                Date(),
                EventType.LLAMADA_REALIZADA_WHATSAPP.label,
                EventTypeShort.CALL.label,
                contactId
            )
            saveHistory(history)

            val phoneNumber = contact.cellphone
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://wa.me/$phoneNumber")
            startActivity(intent)
        }

        whatsappMessage = findViewById(R.id.whatsappMessage)
        whatsappMessage.setOnClickListener {
            val history = History(
                Date(),
                EventType.MENSAJE_WHATSAPP.label,
                EventTypeShort.MESSAGE.label,
                contactId
            )
            saveHistory(history)
            val message = "Hola ${contact.name}!"
            val url =
                "https://api.whatsapp.com/send?phone=${contact.cellphone}&text=${Uri.encode(message)}"
            try {
                val pm = this.packageManager
                pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                intent.putExtra(Intent.EXTRA_TEXT, message)
                startActivity(intent)
            } catch (e: PackageManager.NameNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }

        bottomNavigation = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_navigation_edit -> {
                    if (!contactId.isNullOrEmpty() && contact != null) {
                        val contactJson = gson.toJson(contact)
                        val updateContactScreen = Intent(this, ContactUpdateForm::class.java)
                        updateContactScreen.putExtra("contactId", contactId)
                        updateContactScreen.putExtra("contactInfo", contactJson)
                        startActivity(updateContactScreen)
                    }
                    true
                }
                R.id.bottom_navigation_favorite -> {
                    if (contact != null) {
                        var contactFavorite = contact
                        contactFavorite.favorite = !contactFavorite.favorite
                        firestore.collection("contacts")
                            .document(contactId)
                            .set(contactFavorite)
                            .addOnSuccessListener { doc ->
                                if (contactFavorite.favorite) {
                                    Toast.makeText(
                                        this,
                                        "Guardado como favorito",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                } else {
                                    Toast.makeText(
                                        this,
                                        "Quitado de favoritos",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
//                                val colorStateList = ColorStateList.valueOf(
//                                    ContextCompat.getColor(
//                                        this,
//                                        if (contactFavorite.favorite) R.color.color_primary else android.R.color.black
//                                    )
//                                )
//                                item.iconTintList = colorStateList
                            }
                            .addOnFailureListener { error ->
                                Toast.makeText(this, error.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                    }
                    true
                }
                R.id.bottom_navigation_menu -> {
                    val menuAnchorView = findViewById<View>(R.id.menu_anchor_view)
                    showPopupMenu(contactId,menuAnchorView)
                    true
                }
                R.id.bottom_navigation_share -> {
                    if(contact != null && !contact.cellphone.isNullOrEmpty()) {
                        val menuAnchorView = findViewById<View>(R.id.menu_anchor_view)
                        showPopupMenuShare(contact.cellphone,menuAnchorView)
                    }
                    true
                }
                // Handle other menu items similarly
                else -> true
            }
        }
    }

    private fun showPopupMenu(documentId: String, anchorView: View) {
        val popup = PopupMenu(this, anchorView)
        popup.menuInflater.inflate(R.menu.contact_info_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_delete -> {
                    deleteContact(documentId)
                    true
                }
                R.id.menu_qrcode -> {
                    val qrScreen = Intent(this, ContactQR::class.java)
                    startActivity(qrScreen)
                    true
                }

                else -> false
            }
        }
        popup.show()
    }

    private fun deleteContact(documentId: String) {
        firestore.collection("contacts")
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Contacto eliminado",
                    Toast.LENGTH_SHORT
                ).show()
                val homeScreen = Intent(this, ContactList::class.java)
                startActivity(homeScreen)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun showPopupMenuShare(phone: String, anchorView: View) {
        val popup = PopupMenu(this, anchorView)
        popup.menuInflater.inflate(R.menu.contact_info_share, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_share -> {
                    // Create a new intent
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, phone)
                        type = "text/plain"
                    }

                    // Verify if there's an app to handle the intent
                    if (sendIntent.resolveActivity(packageManager) != null) {
                        // Start the activity with the share intent
                        startActivity(Intent.createChooser(sendIntent, "Compartir por"))
                    }
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun saveHistory(history: History) {
        firestore.collection("history")
            .add(history)
            .addOnSuccessListener { doc ->
            }
            .addOnFailureListener { error ->
            }
    }
}