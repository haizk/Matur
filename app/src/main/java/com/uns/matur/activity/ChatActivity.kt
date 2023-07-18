package com.uns.matur.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.uns.matur.R
import com.uns.matur.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var usersCollection: CollectionReference
    private lateinit var firebaseUser: FirebaseUser


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = Firebase.firestore
        usersCollection = db.collection("users")
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        var userId = intent.getStringExtra("userId")
        var userName = intent.getStringExtra("userName")
        var profileImage = intent.getStringExtra("profileImage")

        usersCollection.whereEqualTo("userId", userId).get().addOnSuccessListener {
            if (it != null) {
                for (document in it) {
                    binding.tvUserName.text = document.data["userName"].toString()
                    if(document.data["profileImage"].toString() != "null") {
                        Glide.with(this).load(document.data["profileImage"].toString()).placeholder(R.drawable.profile_image).into(binding.imgProfile)
                    }
                    else {
                        binding.imgProfile.setImageResource(R.drawable.profile_image)
                    }
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@ChatActivity, UsersActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        })

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }
}