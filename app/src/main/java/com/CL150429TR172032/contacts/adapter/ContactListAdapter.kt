package com.CL150429TR172032.contacts.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.CL150429TR172032.contacts.R
import com.CL150429TR172032.contacts.dto.Contact
import com.CL150429TR172032.contacts.screens.ContactInfo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import com.squareup.picasso.Picasso

open class ContactListAdapter(private var query: Query) :
    FirestoreAdapter<ContactListAdapter.ViewHolder>(query) {

    val gson = Gson()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_row_contact_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val snapshot = getSnapshot(position)
        val currentItem = snapshot.toObject(Contact::class.java)
        Log.i("item ", currentItem.toString())
        if (currentItem != null) {
            val image = if (currentItem.image.isNullOrEmpty()) {
                "https://firebasestorage.googleapis.com/v0/b/contacts-app-db1b7.appspot.com/o/images%2Fdefault-avatar.png?alt=media&token=a8306cf6-c542-40fc-80a2-f65944a5a29e"
            } else {
                currentItem.image
            }
            Picasso.get().load(image).into(holder.imageView)
            val fullName = "${currentItem.name} ${currentItem.lastName}"
            holder.textViewName.text = fullName
            if (currentItem.favorite) {
                holder.favoriteButton.setColorFilter(
                    ContextCompat.getColor(holder.itemView.context, R.color.color_primary)
                )
            } else {
                holder.favoriteButton.setColorFilter(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        androidx.appcompat.R.color.material_grey_600
                    )
                )
            }

            holder.favoriteButton.setOnClickListener {
                var contactFavorite = currentItem
                contactFavorite.favorite = !contactFavorite.favorite

                firestore.collection("contacts")
                    .document(snapshot.id)
                    .set(contactFavorite)
                    .addOnSuccessListener { doc ->
                        if (contactFavorite.favorite) {
                            Toast.makeText(
                                holder.itemView.context,
                                "Guardado como favorito",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                holder.itemView.context,
                                "Quitado de favoritos",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(holder.itemView.context, error.message, Toast.LENGTH_SHORT)
                            .show()
                    }
            }

            holder.itemView.setOnClickListener {
                if (!snapshot.id.isNullOrEmpty()) {
                    val contactJson = gson.toJson(currentItem)
                    val contactInfoScreen = Intent(holder.itemView.context, ContactInfo::class.java)
                    contactInfoScreen.putExtra("contactId", snapshot.id)
                    contactInfoScreen.putExtra("contactInfo", contactJson)
                    holder.itemView.context.startActivity(contactInfoScreen)
                }
            }

            holder.bind(snapshot.id, currentItem)

        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.contactImageCard)
        val textViewName: TextView = itemView.findViewById(R.id.nameCard)
        val favoriteButton: ImageButton = itemView.findViewById(R.id.favoriteButton)

        val menuButton: ImageButton = itemView.findViewById(R.id.menuButton)

        fun bind(documentId: String, currentItem: Contact?) {
            val fullName = "${currentItem?.name} ${currentItem?.lastName}"
            textViewName.text = fullName

            menuButton.setOnClickListener {
                showPopupMenu(documentId)
            }
        }

        private fun showPopupMenu(documentId: String) {
            val popup = PopupMenu(itemView.context, menuButton)
            popup.menuInflater.inflate(R.menu.delete_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_delete -> {
                        deleteContact(documentId)
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
                        itemView.context,
                        "Contacto eliminado",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        itemView.context,
                        "${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}