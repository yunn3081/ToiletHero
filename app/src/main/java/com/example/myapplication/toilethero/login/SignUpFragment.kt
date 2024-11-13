package com.example.myapplication.toilethero.signup

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)

        // init FirebaseAuth 和 Firebase Realtime Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // connect UI
        val emailEditText = view.findViewById<EditText>(R.id.email)
        val passwordEditText = view.findViewById<EditText>(R.id.password) // 新增密碼欄位
        val firstNameEditText = view.findViewById<EditText>(R.id.first_name)
        val lastNameEditText = view.findViewById<EditText>(R.id.last_name)
        val phoneEditText = view.findViewById<EditText>(R.id.phone_number)
        val dobEditText = view.findViewById<EditText>(R.id.date_of_birth)
        val signUpButton = view.findViewById<Button>(R.id.signup_button)

        // sign up on click
        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim() // 使用用戶輸入的密碼
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val dob = dobEditText.text.toString().trim()

            // check if email and password is empty
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Email and password are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // use FirebaseAuth to signup
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // get UID
                        val userId = auth.currentUser?.uid

                        // setup user
                        val user = User(userId, firstName, lastName, email, phone, dob)

                        // save user info to Firebase Realtime Database
                        if (userId != null) {
                            database.child("users").child(userId).setValue(user)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // redirect to  AccountFragment
                                        findNavController().navigate(R.id.action_signUpFragment_to_accountFragment)
                                        Toast.makeText(context, "Sign up successful!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        // if fail to save info, then return with error
                                        Log.e("DatabaseError", "Error: ${task.exception?.message}")
                                        Toast.makeText(
                                            context,
                                            "Failed to save user info: ${task.exception?.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }

                    } else {
                        // if fail to signup, then return with error
                        Log.e("SignUpError", "Error: ${task.exception?.message}")
                        Toast.makeText(
                            context,
                            "Sign up failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        return view
    }

    // create user data type
    data class User(
        val userId: String?,
        val firstName: String,
        val lastName: String,
        val email: String,
        val phone: String,
        val dob: String
    )
}
