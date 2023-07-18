package com.uns.matur.activity

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.uns.matur.R
import com.uns.matur.adapter.ChatAdapter
import com.uns.matur.adapter.UserAdapter
import com.uns.matur.databinding.ActivityChatBinding
import com.uns.matur.model.Chat
import com.uns.matur.model.User

class ChatActivity : AppCompatActivity() {
    private var chatList = ArrayList<Chat>()
    private lateinit var binding: ActivityChatBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var usersCollection: CollectionReference
    private lateinit var firebaseUser: FirebaseUser


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = Firebase.firestore
        usersCollection = db.collection("users")
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        var userId = intent.getStringExtra("userId")
        var userName = intent.getStringExtra("userName")
        var profileImage = intent.getStringExtra("profileImage")

        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        usersCollection.whereEqualTo("userId", userId).get().addOnSuccessListener { snapshot ->
            if (snapshot != null) {
                for (document in snapshot) {
                    binding.tvUserName.text = document.getString("userName")
                    val profileImageUrl = document.getString("profileImage")
                    if (profileImageUrl != null && profileImageUrl != "null") {
                        Glide.with(this@ChatActivity).load(profileImageUrl)
                            .placeholder(R.drawable.profile_image).into(binding.imgProfile)
                    } else {
                        binding.imgProfile.setImageResource(R.drawable.profile_image)
                    }
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@ChatActivity, UsersActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        })

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnSendMessage.setOnClickListener {
            val message = binding.Message.text.toString().trim()

            if (message.isEmpty()) {
                Toast.makeText(applicationContext, "Message is empty", Toast.LENGTH_SHORT).show()
            } else {
                val senderId = firebaseUser.uid
                val receiverId = intent.getStringExtra("userId") ?: ""
                sendMessage(senderId, receiverId, message)
            }
        }

        // harusnya manggil readMessage doang
        if (userId != null) {
            readMessage(firebaseUser.uid, userId)
        }
    }

    private fun sendMessage(senderId: String, receiverId: String, message: String) {
        val messageMap = hashMapOf(
            "senderId" to senderId,
            "receiverId" to receiverId,
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )

        val messagesCollection = db.collection("messages")

        messagesCollection
            .add(messageMap)
            .addOnSuccessListener { documentReference ->
                val messageId = documentReference.id
                showToast("Message sent successfully!")
                binding.Message.setText("")
            }
            .addOnFailureListener { e ->
                showToast("Failed to send message. Please try again.")
                Log.e(TAG, "Error sending message: ${e.message}", e)
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun readMessage(senderId: String, receiverId: String) {
        val messagesCollection = db.collection("messages")

        messagesCollection
            .whereEqualTo("senderId", senderId)
            .whereIn("receiverId", listOf(senderId, receiverId))
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                chatList.clear()
                for (document in querySnapshot) {
                    val chat = document.toObject(Chat::class.java)
                    chatList.add(chat)
                }

                val chatAdapter = ChatAdapter(this@ChatActivity, chatList)
                binding.chatRecyclerView.adapter = chatAdapter

                // Scroll to the last message
                binding.chatRecyclerView.scrollToPosition(chatList.size - 1)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error reading messages: ${e.message}", e)
            }
    }

}