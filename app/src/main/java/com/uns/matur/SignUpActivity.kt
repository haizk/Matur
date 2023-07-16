package com.android.matur

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.matur.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.regex.Matcher
import java.util.regex.Pattern


class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnSignUp.setOnClickListener {
            val userName = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (TextUtils.isEmpty(userName)) {
                Toast.makeText(applicationContext, "Username is required!", Toast.LENGTH_SHORT).show()
            }
            else if (isUsernameValid(userName)) {
                Toast.makeText(applicationContext, "Username badly formatted!", Toast.LENGTH_SHORT).show()
            }
            else if (TextUtils.isEmpty(email)) {
                Toast.makeText(applicationContext, "Email is required!", Toast.LENGTH_SHORT).show()
            }
            else if (isEmailValid(email)) {
                Toast.makeText(applicationContext, "Email badly formatted!", Toast.LENGTH_SHORT).show()
            }
            else if (TextUtils.isEmpty(password)) {
                Toast.makeText(applicationContext, "Password is required!", Toast.LENGTH_SHORT).show()
            }
            else if (isPasswordValid(password)) {
                Toast.makeText(applicationContext, "Password badly formatted!", Toast.LENGTH_SHORT).show()
            }
            else if (TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(applicationContext, "Confirm password is required!", Toast.LENGTH_SHORT).show()
            }
            else if (password != confirmPassword) {
                Toast.makeText(applicationContext, "Password not match!", Toast.LENGTH_SHORT).show()
            }
            else {
                registerUser(userName, email, password)
                Toast.makeText(applicationContext, "Sign up success!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(userName: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { it ->
            if (it.isSuccessful) {
                val user: FirebaseUser? = auth.currentUser
                val userId: String = user!!.uid

                databaseReference =
                    FirebaseDatabase.getInstance().getReference("Users").child(userId)

                val hashMap: HashMap<String, String> = HashMap()
                hashMap["userId"] = userId
                hashMap["userName"] = userName
                hashMap["profileImage"] = ""

                databaseReference.setValue(hashMap).addOnCompleteListener(this) {
                    Log.d("SignUpActivity", "createUserWithEmail:success")
                    if (it.isSuccessful) {
                        val intent = Intent(this@SignUpActivity, HomeActivity::class.java)
                        startActivity(intent)
                    }
                }
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