package com.uns.matur.activity

import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
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
                    binding.etUserName.text = username
                    binding.etEditUserName.setText(username)
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

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnEditProfile.setOnClickListener {
            if(binding.btnEditProfile.text == getString(R.string.edit_profile)) {
                binding.etUserName.visibility = android.view.View.GONE
                binding.etEditUserName.visibility = android.view.View.VISIBLE
                binding.btnEditProfile.text = getString(R.string.save_profile)
                binding.btnEditProfile.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@ProfileActivity, R.color.colorLightGreen))
                binding.userImage.setOnClickListener {
                    Toast.makeText(this@ProfileActivity, "Change profile image", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                if(binding.etEditUserName.text.toString().isNotEmpty() && binding.etEditUserName.text.toString().length >= 3) {
                    usersCollection.whereEqualTo("userName", binding.etEditUserName.text.toString())
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                usersCollection.whereEqualTo("userId", firebaseUser.uid)
                                    .get()
                                    .addOnSuccessListener { documents2 ->
                                        if(documents.documents[0].getString("userName") == documents2.documents[0].getString("userName")) {
                                            binding.etUserName.visibility = View.VISIBLE
                                            binding.etEditUserName.visibility = View.GONE
                                            binding.btnEditProfile.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@ProfileActivity, R.color.colorLightYellow))
                                            binding.userImage.setOnClickListener(null)
                                            usersCollection.whereEqualTo("userId", firebaseUser.uid)
                                                .get()
                                                .addOnSuccessListener { documents3 ->
                                                    if (documents3 != null && !documents3.isEmpty) {
                                                        //change profile image
                                                    } else {
                                                        Toast.makeText(this@ProfileActivity, "No such document", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                                .addOnFailureListener { exception ->
                                                    Toast.makeText(this@ProfileActivity, "get failed with $exception", Toast.LENGTH_SHORT).show()
                                                }
                                        } else {
                                            binding.etEditUserName.error = "Username already exists"
                                            binding.etEditUserName.requestFocus()
                                            Toast.makeText(this@ProfileActivity, "Username already exists", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                binding.etUserName.visibility = android.view.View.VISIBLE
                                binding.etEditUserName.visibility = android.view.View.GONE
                                binding.btnEditProfile.text = getString(R.string.edit_profile)
                                binding.btnEditProfile.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@ProfileActivity, R.color.colorLightYellow))
                                binding.userImage.setOnClickListener(null)
                                usersCollection.whereEqualTo("userId", firebaseUser.uid)
                                    .get()
                                    .addOnSuccessListener { documents2 ->
                                        if (documents2 != null && !documents2.isEmpty) {
                                            val userDocument = documents2.first()
                                            val username = binding.etEditUserName.text.toString()
                                            userDocument.reference.update("userName", username)
                                            binding.etUserName.text = username
                                            Toast.makeText(
                                                this@ProfileActivity,
                                                "Username: $username",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            //change profile image
                                        } else {
                                            Toast.makeText(this@ProfileActivity, "No such document", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(this@ProfileActivity, "get failed with $exception", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this@ProfileActivity, "get failed with $exception", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    binding.etEditUserName.error = "Username must be more than 3 characters"
                    binding.etEditUserName.requestFocus()
                    Toast.makeText(this@ProfileActivity, "Username must be more than 3 characters", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
