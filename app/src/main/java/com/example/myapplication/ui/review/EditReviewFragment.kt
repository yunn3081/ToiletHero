package com.example.myapplication.ui.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditReviewFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var reviewId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_review, container, false)

        // 初始化 Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // 連接 UI 元素
        val roomIdEditText = view.findViewById<EditText>(R.id.room_id_edit)
        val commentEditText = view.findViewById<EditText>(R.id.comment_edit)
        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar_edit)
        val saveButton = view.findViewById<Button>(R.id.save_button)

        // 從 Bundle 中提取數據
        reviewId = arguments?.getString("reviewId")
        val roomId = arguments?.getString("roomId")
        val comment = arguments?.getString("comment")
        val star = arguments?.getFloat("star")

        // 設置初始數據
        roomIdEditText.setText(roomId)
        commentEditText.setText(comment)
        ratingBar.rating = star ?: 0f

        // 保存按鈕的點擊事件
        saveButton.setOnClickListener {
            val updatedComment = commentEditText.text.toString()
            val updatedStar = ratingBar.rating

            // 更新評論
            val updates = mapOf("comment" to updatedComment, "star" to updatedStar)
            reviewId?.let {
                database.child("reviews").child(auth.currentUser!!.uid).child(it).updateChildren(updates)
            }
        }

        return view
    }
}
