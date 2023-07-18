package com.uns.matur.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.uns.matur.R
import com.uns.matur.model.Chat
import de.hdodenhof.circleimageview.CircleImageView

class ChatAdapter(private val context: Context, private val chatList: ArrayList<Chat>) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    private val messageTypeLeft = 0
    private val messageTypeRight = 1
    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = if (viewType == messageTypeRight) {
            LayoutInflater.from(parent.context).inflate(R.layout.item_right, parent, false)
        } else {
            LayoutInflater.from(parent.context).inflate(R.layout.item_left, parent, false)
        }
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = chatList[position]
        holder.txtMessage.text = chat.message

        val db = Firebase.firestore.collection("users")
        db.whereEqualTo("userId", chat.senderId).get().addOnSuccessListener { snapshot ->
            if (snapshot != null) {
                for (document in snapshot) {
                    Glide.with(context).load(document.getString("profileImage")).placeholder(R.drawable.profile_image).into(holder.imgUser)
                }
            }
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtMessage: TextView = itemView.findViewById(R.id.tvMessage)
        val imgUser: CircleImageView = itemView.findViewById(R.id.userImage)
    }

    override fun getItemViewType(position: Int): Int {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        return if (chatList[position].senderId == firebaseUser?.uid) {
            messageTypeRight
        } else {
            messageTypeLeft
        }
    }
}
