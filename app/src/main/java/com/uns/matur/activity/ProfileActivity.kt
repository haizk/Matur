package com.uns.matur.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.uns.matur.R
import com.uns.matur.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        db = Firebase.firestore

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val usersCollection = db.collection("users")

        usersCollection.whereEqualTo("userId", firebaseUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    val userDocument = documents.first()
                    val username = userDocument.getString("userName")
                    binding.etUserName.setText(username)
                    if(userDocument.getString("profileImage").isNullOrEmpty()) {
                        binding.userImage.setImageResource(R.drawable.profile_image)
                    } else {
                        Glide.with(this@ProfileActivity).load(userDocument.getString("profileImage")).into(binding.userImage)
                    }
                    Toast.makeText(this@ProfileActivity, "Username: $username", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProfileActivity, "No such document", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this@ProfileActivity, "get failed with $exception", Toast.LENGTH_SHORT).show()
            }

        binding.imgBack.setOnClickListener() {
            onBackPressed()
        }
    }
}
