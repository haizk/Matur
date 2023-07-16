package com.uns.matur.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.uns.matur.R
import com.uns.matur.adapter.UserAdapter
import com.uns.matur.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.uns.matur.databinding.ActivityUsersBinding

class UsersActivity : AppCompatActivity() {
    var userList = ArrayList<User>()
    private lateinit var binding: ActivityUsersBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        db = Firebase.firestore

        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.userRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        getUsersList()

        binding.imgBack.setOnClickListener() {
            onBackPressed()
        }

        binding.imgProfile.setOnClickListener() {
            val intent = Intent(this@UsersActivity, ProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }
    }

    fun getUsersList() {
        val usersCollection = db.collection("users")
        usersCollection.get().addOnSuccessListener { documents ->
            for (document in documents) {
                val user = document.toObject(User::class.java)
                if (user != null) {
                    userList.add(user)
                }
            }
            val userAdapter = UserAdapter(this@UsersActivity, userList)
            binding.userRecyclerView.adapter = userAdapter
        }
    }
}