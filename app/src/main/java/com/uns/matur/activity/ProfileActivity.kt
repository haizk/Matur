package com.uns.matur.activity

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.uns.matur.R
import com.uns.matur.databinding.ActivityProfileBinding
import java.util.regex.Matcher
import java.util.regex.Pattern

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var usersCollection: CollectionReference
    private lateinit var firebaseUser: FirebaseUser

    private var pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
//            Toast.makeText(this@ProfileActivity, uri.toString(), Toast.LENGTH_SHORT).show()
            //binding.userImage.setImageURI(uri)
            Glide.with(this@ProfileActivity).load(uri).into(binding.userImage)
            binding.userImage.tag = uri
//            Toast.makeText(this@ProfileActivity, binding.userImage.tag.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        db = Firebase.firestore

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        usersCollection = db.collection("users")

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@ProfileActivity, UsersActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        })

        usersCollection.whereEqualTo("userId", firebaseUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    val userDocument = documents.first()
                    val username = userDocument.getString("userName")
                    binding.etUserName.text = username
                    binding.etEditUserName.setText(username)
                    binding.userImage.tag = userDocument.getString("profileImage")
                    //Toast.makeText(this@ProfileActivity, "Username: ${userDocument.getString("profileImage")}", Toast.LENGTH_SHORT).show()
                    if(userDocument.getString("profileImage").isNullOrEmpty()) {
                        binding.userImage.setImageResource(R.drawable.profile_image)
                    } else {
                        Glide.with(this@ProfileActivity).load(userDocument.getString("profileImage")).into(binding.userImage)
                    }
                    //Toast.makeText(this@ProfileActivity, "Username: $username", Toast.LENGTH_SHORT).show()
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

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this@ProfileActivity, "Logout", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@ProfileActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        binding.btnEditProfile.setOnClickListener {
            if(binding.btnEditProfile.text == getString(R.string.edit_profile)) {
                binding.etUserName.visibility = View.GONE
                binding.etEditUserName.visibility = View.VISIBLE
                binding.btnEditProfile.text = getString(R.string.save_profile)
                binding.btnEditProfile.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@ProfileActivity, R.color.colorLightGreen))
                binding.userImage.setOnClickListener {
                    chooseImage()
                    Toast.makeText(this@ProfileActivity, "Change profile image", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                if(binding.etEditUserName.text.toString().isNotEmpty() && isUsernameValid(binding.etEditUserName.text.toString())) {
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
                                            binding.btnEditProfile.text = getString(R.string.edit_profile)
                                            binding.btnEditProfile.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@ProfileActivity, R.color.colorLightYellow))
                                            binding.userImage.setOnClickListener(null)
                                            usersCollection.whereEqualTo("userId", firebaseUser.uid)
                                                .get()
                                                .addOnSuccessListener { documents3 ->
                                                    if (documents3 != null && !documents3.isEmpty) {
                                                        uploadImage()
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
                                binding.etUserName.visibility = View.VISIBLE
                                binding.etEditUserName.visibility = View.GONE
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
                                            uploadImage()
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
                    binding.etEditUserName.error = "Username badly formatted!"
                    binding.etEditUserName.requestFocus()
                    Toast.makeText(this@ProfileActivity, "Username badly formatted!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isUsernameValid(username: String): Boolean {
        val expression = "^[a-z0-9_-]{3,15}$"
        val pattern: Pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(username)
        return matcher.matches()
    }

    private fun chooseImage() {
        pickImage.launch("image/*")
    }


    private fun uploadImage() {
        val selectedImageUri = binding.userImage.tag.toString()
        if (selectedImageUri != "") {
            val dialog = Dialog(this@ProfileActivity)
            dialog.setContentView(R.layout.dialog_uploading_image)
            dialog.setCancelable(false)
            dialog.show()

            val storageRef = FirebaseStorage.getInstance().reference.child("profileImages/${FirebaseAuth.getInstance().currentUser!!.uid}")
            val uploadTask = storageRef.putFile(selectedImageUri.toUri())
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                storageRef.downloadUrl
            }.addOnCompleteListener { task ->
                dialog.dismiss()
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    binding.userImage.tag = downloadUri.toString()
                    usersCollection.whereEqualTo("userId", firebaseUser.uid)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (documents != null && !documents.isEmpty) {
                                val userDocument = documents.first()
                                userDocument.reference.update("profileImage", downloadUri.toString())
                                Toast.makeText(this@ProfileActivity, userDocument.getString("profileImage"), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@ProfileActivity, "No such document", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this@ProfileActivity, "get failed with $exception", Toast.LENGTH_SHORT).show()
                        }
                    Glide.with(this@ProfileActivity).load(downloadUri).into(binding.userImage)
                } else {
                    Toast.makeText(this@ProfileActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(this@ProfileActivity, "No image selected", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this@ProfileActivity, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }
}
