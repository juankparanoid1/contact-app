package com.CL150429TR172032.contacts.screens

import android.Manifest
import android.content.ContentProviderOperation
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.Data
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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


class ContactForm : AppCompatActivity() {

    private lateinit var nameInputText: TextInputEditText
    private lateinit var lastNameInputText: TextInputEditText
    private lateinit var nickNameInputText: TextInputEditText
    private lateinit var cellphoneInputText: TextInputEditText
    private lateinit var emailInputText: TextInputEditText
    private lateinit var websiteInputText: TextInputEditText

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var contactUserImage: ImageView
    private lateinit var selectedImageUri: Uri
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_form)

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

        saveButton = findViewById(R.id.saveButton)

        saveButton.setOnClickListener {
            saveNewContact()
            val homeScreen = Intent(this, ContactList::class.java)
            startActivity(homeScreen)
        }

        cancelButton = findViewById(R.id.cancelButton)

        cancelButton.setOnClickListener {
            val homeScreen = Intent(this, ContactList::class.java)
            startActivity(homeScreen)
        }
    }

    private fun saveNewContact() {
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
            val imageRef = storage.reference.child("images/${selectedImageUri.lastPathSegment}")

            imageRef.putFile(selectedImageUri)
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
                        saveContactToFirestore(contact)
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
                "",
                false,
                nameLowerCase
            )
            Log.i("contact", contact.toString());
            saveContactToFirestore(contact)
        }
    }

    private fun saveContactToFirestore(contact: Contact) {
        firestore.collection("contacts")
            .add(contact)
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

    private fun addContact(firstName: String, lastName: String, number: String) {
        val operationList = ArrayList<ContentProviderOperation>()
        operationList.add(
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )
        operationList.add(
            ContentProviderOperation.newInsert(Data.CONTENT_URI)
                .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                .withValue(StructuredName.GIVEN_NAME, firstName)
                .withValue(StructuredName.FAMILY_NAME, lastName)
                .build()
        )
        operationList.add(
            ContentProviderOperation.newInsert(Data.CONTENT_URI)
                .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                .withValue(Phone.NUMBER, number)
                .withValue(Phone.TYPE, Phone.TYPE_HOME)
                .build()
        )
        try {
            val results = contentResolver.applyBatch(ContactsContract.AUTHORITY, operationList)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}