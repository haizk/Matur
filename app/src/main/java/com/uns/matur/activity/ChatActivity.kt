package com.uns.matur.activity

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.google.firebase.messaging.FirebaseMessaging
import com.uns.matur.R
import com.uns.matur.adapter.ChatAdapter
import com.uns.matur.databinding.ActivityChatBinding
import com.uns.matur.model.Chat

class ChatActivity : AppCompatActivity() {
    private var chatList = ArrayList<Chat>()
    private lateinit var binding: ActivityChatBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var usersCollection: CollectionReference
    private lateinit var firebaseUser: FirebaseUser
    //private var topic = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = Firebase.firestore
        usersCollection = db.collection("users")
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val userId = intent.getStringExtra("userId")

        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)

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
                Toast.makeText(applicationContext, "Serat kosong!", Toast.LENGTH_SHORT).show()
            } else {
                val senderId = firebaseUser.uid
                val receiverId = intent.getStringExtra("userId") ?: ""
                sendMessage(senderId, receiverId, message)
            }
        }

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
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "Serat sampun kekintun!", Toast.LENGTH_SHORT).show()
                binding.Message.setText("")
            }
            .addOnFailureListener { e ->
                Toast.makeText(applicationContext, "Mboten saged ngintun serat. Cobi malih.", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error sending message: ${e.message}", e)
            }
    }

    private fun readMessage(senderId: String, receiverId: String) {
        val messagesCollection = db.collection("messages")

        messagesCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Error getting messages: ${e.message}", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    chatList.clear()
                    for (document in snapshot) {
                        val chat = document.toObject(Chat::class.java)
                        if (chat.senderId == senderId && chat.receiverId == receiverId ||
                            chat.senderId == receiverId && chat.receiverId == senderId) {
                            chatList.add(chat)
                        }
                    }
                    val chatAdapter = ChatAdapter(this, chatList)
                    binding.chatRecyclerView.adapter = chatAdapter
                }
            }
    }

    fun sendNotification() {
        val serverKey = "YOUR_SERVER_KEY"
        val message = "Hello, World!"
        val tokens = listOf("DEVICE_TOKEN_1", "DEVICE_TOKEN_2") // Add all target device tokens here
        val title = "YOUR_TITLE"
        val topic = "YOUR_TOPIC_NAME"
    }
}