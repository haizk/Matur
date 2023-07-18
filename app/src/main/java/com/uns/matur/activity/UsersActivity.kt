package com.uns.matur.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.uns.matur.R
import com.uns.matur.adapter.UserAdapter
import com.uns.matur.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.uns.matur.databinding.ActivityUsersBinding

class UsersActivity : AppCompatActivity() {
    private var userList = ArrayList<User>()
    private lateinit var binding: ActivityUsersBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser != null) {
            Toast.makeText(this@UsersActivity, "Sugeng rawuh, ${currentUser.email}", Toast.LENGTH_SHORT).show()
        }

        db = Firebase.firestore

        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.userRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val usersCollection = db.collection("users")
        usersCollection.whereEqualTo("userId", firebaseUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    val userDocument = documents.first()
                    if(userDocument.getString("profileImage").isNullOrEmpty()) {
                        binding.imgProfile.setImageResource(R.drawable.profile_image)
                    } else {
                        Glide.with(this@UsersActivity).load(userDocument.getString("profileImage")).into(binding.imgProfile)
                    }
                    binding.imgProfile.tag = userDocument.getString("profileImage")
                } else {
                    Toast.makeText(this@UsersActivity, "No such document", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this@UsersActivity, "get failed with $exception", Toast.LENGTH_SHORT).show()
            }

        getUsersList()

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.imgProfile.setOnClickListener {
            val intent = Intent(this@UsersActivity, ProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }
    }

    private fun getUsersList() {
        val usersCollection = db.collection("users")
        usersCollection.get().addOnSuccessListener { documents ->
            for (document in documents) {
                if(document.getString("userId") != firebaseUser.uid) {
                    val user = document.toObject(User::class.java)
                    userList.add(user)
                }
            }
            val userAdapter = UserAdapter(this@UsersActivity, userList)
            binding.userRecyclerView.adapter = userAdapter
        }
    }
}