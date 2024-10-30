package com.example.myapplication.toilethero.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CreateReviewFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_review, container, false)

        // 初始化 FirebaseAuth 和 Firebase Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // 連接 UI 元素
        val roomEditText = view.findViewById<EditText>(R.id.room_id_edit_text)
        val commentEditText = view.findViewById<EditText>(R.id.comment_edit_text)
        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        val submitButton = view.findViewById<Button>(R.id.submit_review_button)

        // 設置提交按鈕的點擊事件
        submitButton.setOnClickListener {
            val roomId = roomEditText.text.toString().trim()
            val comment = commentEditText.text.toString().trim()
            val starRating = ratingBar.rating

            if (roomId.isNotEmpty() && comment.isNotEmpty()) {
                // 保存評論到 Firebase
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    val reviewId = database.child("reviews").push().key ?: return@setOnClickListener
                    val review = Review(reviewId, roomId, comment, starRating, userId)

                    database.child("reviews").child(userId).child(reviewId).setValue(review)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Review created successfully!", Toast.LENGTH_SHORT).show()
                            // 返回到 ReviewFragment
                            findNavController().navigate(R.id.action_createReviewFragment_to_reviewFragment)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Failed to create review: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(context, "Room ID and comment are required", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
