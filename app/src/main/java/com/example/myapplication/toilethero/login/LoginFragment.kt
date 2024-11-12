package com.example.myapplication.toilethero.login

import android.os.Bundle
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

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private var returnToReviewPage: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        returnToReviewPage = arguments?.getBoolean("returnToReviewPage") ?: false

        // If already logged in, navigate based on back stack presence
        if (currentUser != null) {
            navigateAfterLogin()
            return view
        }

        // Connect UI elements
        val emailEditText = view.findViewById<EditText>(R.id.email)
        val passwordEditText = view.findViewById<EditText>(R.id.password)
        val loginButton = view.findViewById<Button>(R.id.login_button)
        val signupButton = view.findViewById<Button>(R.id.signup_button)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                            navigateAfterLogin()
                        } else {
                            Toast.makeText(
                                context,
                                "Login Failed: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        signupButton.setOnClickListener {
            findNavController().navigate(R.id.action_notificationsFragment_to_signUpFragment)
        }

        return view
    }

    private fun navigateAfterLogin() {
        if (returnToReviewPage) {
            // If there is a previous fragment, pop back to it
            findNavController().popBackStack()
        } else {
            // Otherwise, navigate to the account info page
            findNavController().navigate(R.id.action_notificationsFragment_to_accountFragment)
        }
    }
}
