package com.uns.matur.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.uns.matur.R
import com.uns.matur.databinding.ActivitySignUpBinding
import java.util.regex.Matcher
import java.util.regex.Pattern

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(intent)
                finish()
            }
        })

        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            finish()
        }

        binding.btnSignUp.setOnClickListener {
            val userName = binding.etName.text.toString().lowercase()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (TextUtils.isEmpty(userName)) {
                binding.etName.error = "Asmanipun kedah diisi!"
                binding.etName.requestFocus()
            }
            else if (isUsernameValid(userName)) {
                binding.etName.error = "Format asmanipun mboten cocog!"
                binding.etName.requestFocus()
            }
            else if (TextUtils.isEmpty(email)) {
                binding.etEmail.error = "Email kedah diisi!"
                binding.etEmail.requestFocus()

            }
            else if (isEmailValid(email)) {
                binding.etEmail.error = "Format email mboten cocog!"
                binding.etEmail.requestFocus()
            }
            else if (TextUtils.isEmpty(password)) {
                binding.etPassword.error = "Password kedah diisi!"
                binding.etPassword.requestFocus()
            }
            else if (isPasswordValid(password)) {
                binding.etPassword.error = "Format password mboten cocog!"
                binding.etPassword.requestFocus()
            }
            else if (TextUtils.isEmpty(confirmPassword)) {
                binding.etConfirmPassword.error = "Konfirmasi Password kedah diisi!"
                binding.etConfirmPassword.requestFocus()
            }
            else if (password != confirmPassword) {
                binding.etConfirmPassword.error = "Password benten!"
                binding.etConfirmPassword.requestFocus()
            }
            else {
                val query = db.collection("users").whereEqualTo("userName", userName)
                query.get().addOnSuccessListener {
                    if (it.isEmpty) {
                        registerUser(userName, email, password)
                    } else {
                        binding.etName.error = "Asmanipun sampun kedaptar!"
                        binding.etName.requestFocus()
                    }
                }
            }
        }
    }

    private fun registerUser(userName: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                val user: FirebaseUser? = auth.currentUser
                val userId: String = user!!.uid

                val hashMap = hashMapOf(
                    "userId" to userId,
                    "userName" to userName,
                    "profileImage" to ""
                )

                db.collection("users")
                    .add(hashMap)
                    .addOnSuccessListener {
                        Toast.makeText(applicationContext, "Ndaptaripun kasembadan!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(applicationContext, "Ndaptaripun gagal!", Toast.LENGTH_SHORT).show()
                    }

            } else if(it.exception.toString().contains("Email sampun kedaptar ing akun liyane!")) {
                binding.etEmail.error = "Email sampun kedaptar!"
                binding.etEmail.requestFocus()
            } else {
                Toast.makeText(applicationContext, "Ndaptaripun gagal!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isEmailValid(email: String): Boolean {
        val expression = "^[\\w.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern: Pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(email)
        return !matcher.matches()
    }

    private fun isUsernameValid(username: String): Boolean {
        val expression = "^[a-z0-9_-]{3,15}$"
        val pattern: Pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(username)
        return !matcher.matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        val expression = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=]).{8,}\$"
        val pattern: Pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(password)
        return !matcher.matches()
    }
}