package com.example.myapplication.toilethero.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.toilethero.review.ReviewFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AccountFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        // init FirebaseAuth and FirebaseDatabase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // get greeting TextView
        val greetingText = view.findViewById<TextView>(R.id.greetingText)

        val userId = auth.currentUser?.uid
        userId?.let {
            database.child("users").child(it).child("firstName").get().addOnSuccessListener { snapshot ->
                val firstName = snapshot.getValue(String::class.java) ?: "User"
                val greeting = getString(R.string.greeting, firstName)
                greetingText.text = greeting
            }.addOnFailureListener {
                greetingText.text = getString(R.string.greeting, "User")
            }
        }

        // setting button
        val settingsButton = view.findViewById<ImageButton>(R.id.settings_button)
        settingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment_to_accountInfoFragment)
        }

        // load ReviewFragment
        childFragmentManager.beginTransaction()
            .replace(R.id.review_fragment_container, ReviewFragment())
            .commit()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }
}
