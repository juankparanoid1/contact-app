package com.CL150429TR172032.contacts.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.CL150429TR172032.contacts.R
import com.CL150429TR172032.contacts.dto.Contact
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.google.gson.Gson
import com.squareup.picasso.Picasso

class ContactUpdateForm : AppCompatActivity() {

    private lateinit var nameInputText: TextInputEditText
    private lateinit var lastNameInputText: TextInputEditText
    private lateinit var nickNameInputText: TextInputEditText
    private lateinit var cellphoneInputText: TextInputEditText
    private lateinit var emailInputText: TextInputEditText
    private lateinit var websiteInputText: TextInputEditText

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var contactUserImage: ImageView
    private var selectedImageUri: Uri? = null
    private lateinit var updateButton: Button
    private lateinit var cancelButton: Button

    private lateinit var contactId: String
    private lateinit var currentContact: Contact
    val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_update_form)

        contactId = intent.getStringExtra("contactId") ?: ""

        Log.i("contactId ", contactId)

        val contactJson = intent.getStringExtra("contactInfo")
        currentContact = gson.fromJson(contactJson, Contact::class.java)

        Log.i("contact gson ", currentContact.toString())

        nameInputText = findViewById(R.id.nameInputText)
        lastNameInputText = findViewById(R.id.lastNameInputText)
        nickNameInputText = findViewById(R.id.nickNameInputText)
        cellphoneInputText = findViewById(R.id.cellphoneInputText)
        emailInputText = findViewById(R.id.emailInputText)
        websiteInputText = findViewById(R.id.websiteInputText)

        contactUserImage = findViewById(R.id.contactUserImage)

        contactUserImage.setOnClickListener {
            checkPermissionAndSelectImage()
        }

        updateButton = findViewById(R.id.updateButton)

        updateButton.setOnClickListener {
            updateContact()
            val homeScreen = Intent(this, ContactList::class.java)
            startActivity(homeScreen)
        }

        cancelButton = findViewById(R.id.cancelUpdateButton)

        cancelButton.setOnClickListener {
            val homeScreen = Intent(this, ContactList::class.java)
            startActivity(homeScreen)
        }

        if (currentContact != null) {
            fillContactForm(currentContact)
        }
    }

    private fun fillContactForm(contact: Contact) {
        contactUserImage = findViewById(R.id.contactUserImage)
        val image = if (contact.image.isNullOrEmpty()) {
            "https://firebasestorage.googleapis.com/v0/b/contacts-app-db1b7.appspot.com/o/images%2Fdefault-avatar.png?alt=media&token=a8306cf6-c542-40fc-80a2-f65944a5a29e"
        } else {
            contact.image
        }
        Picasso.get().load(image).into(contactUserImage)
        nameInputText.setText(contact.name)
        lastNameInputText.setText(contact.lastName)
        nickNameInputText.setText(contact.nickName)
        cellphoneInputText.setText(contact.cellphone)
        emailInputText.setText(contact.email)
        websiteInputText.setText(contact.website)
    }

    private fun updateContact() {
        val name = nameInputText.text.toString()
        val lastName = lastNameInputText.text.toString()
        val nickname = nickNameInputText.text.toString()
        val cellphone = cellphoneInputText.text.toString()
        val email = emailInputText.text.toString()
        val website = websiteInputText.text.toString()
        val firstName = name.takeWhile { it != ' ' }
        val nameLowerCase = firstName.lowercase()

        firestore = Firebase.firestore
        storage = Firebase.storage

        if (selectedImageUri != null) {
            val imageRef = storage.reference.child("images/${selectedImageUri!!.lastPathSegment}")

            imageRef.putFile(selectedImageUri!!)
                .addOnSuccessListener { _ ->
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val contact = Contact(
                            "",
                            name,
                            lastName,
                            nickname,
                            cellphone,
                            email,
                            website,
                            uri.toString(), // Image URL
                            false,
                            nameLowerCase
                        )
                        Log.i("contact", contact.toString());
                        updateContactData(contact)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error uploading image", e)
                    Toast.makeText(
                        this,
                        "Error uploading image",
                        Toast.LENGTH_SHORT
                    ).show()
                }

        } else {
            val contact = Contact(
                "",
                name,
                lastName,
                nickname,
                cellphone,
                email,
                website,
                currentContact.image,
                currentContact.favorite,
                nameLowerCase
            )
            Log.i("contact", contact.toString());
            updateContactData(contact)
        }
    }

    private fun updateContactData(contact: Contact) {
        firestore.collection("contacts")
            .document(contactId)
            .set(contact)
            .addOnSuccessListener { doc ->
                Toast.makeText(this, "Contacto guardado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
            }
        //addContact(contact.name, contact.lastName, contact.cellphone)
    }

    private fun checkPermissionAndSelectImage() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openImagePicker()
        } else {
            requestStoragePermission()
        }
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
            REQUEST_IMAGE_PERMISSION
        )
    }

    private fun openImagePicker() {
        getContent.launch("image/*")
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = uri
                contactUserImage.setImageURI(uri)
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_IMAGE_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    openImagePicker()
                } else {
                    Toast.makeText(
                        this,
                        "Permission denied. Cannot select image.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }

    companion object {
        private const val TAG = "ContactForm"
        private const val REQUEST_IMAGE_PERMISSION = 101
    }

}