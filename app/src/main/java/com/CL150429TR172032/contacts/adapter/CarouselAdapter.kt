package com.CL150429TR172032.contacts.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.CL150429TR172032.contacts.R
import com.CL150429TR172032.contacts.dto.Contact
import com.CL150429TR172032.contacts.screens.ContactInfo
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import com.squareup.picasso.Picasso

open class CarouselAdapter(private var query: Query) : FirestoreAdapter<CarouselAdapter.ViewHolder>(query) {

    val gson = Gson()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val snapshot = getSnapshot(position)
        val currentItem = snapshot.toObject(Contact::class.java)
        if (currentItem != null) {
            val image = if (currentItem.image.isNullOrEmpty()) {
                "https://firebasestorage.googleapis.com/v0/b/contacts-app-db1b7.appspot.com/o/images%2Fdefault-avatar.png?alt=media&token=a8306cf6-c542-40fc-80a2-f65944a5a29e"
            } else {
                currentItem.image
            }
            Picasso.get().load(image).into(holder.imageView)
            holder.textViewName.text = currentItem.name
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
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.contactImage)
        val textViewName: TextView = itemView.findViewById(R.id.contactName)
    }
}