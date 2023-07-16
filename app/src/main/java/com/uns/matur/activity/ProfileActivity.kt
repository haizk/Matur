package com.uns.matur.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.uns.matur.R
import com.uns.matur.databinding.ActivityProfileBinding
import com.uns.matur.model.User

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.uid)
        }

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    binding.etUserName.setText(user.userName)
                    if (user.profileImage.isEmpty()) {
                        binding.userImage.setImageResource(R.drawable.profile_image)
                    } else {
                        Glide.with(this@ProfileActivity).load(user.profileImage).into(binding.userImage)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }
        })

        binding.imgBack.setOnClickListener() {
            onBackPressed()
        }
    }
}
