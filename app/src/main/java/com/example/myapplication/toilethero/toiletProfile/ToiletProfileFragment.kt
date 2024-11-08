package com.example.myapplication.toilethero.toiletProfile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ToiletProfileFragment : Fragment() {

    private lateinit var toiletName: TextView
    private lateinit var toiletRoomNumber: TextView
    private lateinit var toiletAddress: TextView
    private lateinit var toiletRating: RatingBar
    private lateinit var ratingStats: TextView
    private lateinit var reviewTitle: EditText
    private lateinit var reviewBody: EditText
    private lateinit var submitReviewButton: Button
    private lateinit var database_restrooms: DatabaseReference
    private lateinit var database_reviews: DatabaseReference



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_toilet_profile, container, false)

        toiletName = view.findViewById(R.id.toilet_name)
        toiletRoomNumber = view.findViewById(R.id.toilet_roomNumber)
        toiletAddress = view.findViewById(R.id.toilet_address)
        toiletRating = view.findViewById(R.id.toilet_rating)
        ratingStats = view.findViewById(R.id.rating_stats)
        reviewTitle = view.findViewById(R.id.review_title)
        reviewBody = view.findViewById(R.id.review_body)
        submitReviewButton = view.findViewById(R.id.submit_review_button)

        // 獲取 roomID
        val roomID = arguments?.getString("roomID") ?: return view

        // 初始化 Firebase Database
        database_restrooms = FirebaseDatabase.getInstance().getReference("restrooms")
        database_reviews = FirebaseDatabase.getInstance().getReference("reviews")

        // 從 Firebase 獲取廁所資訊
        fetchToiletDetails(roomID)

        // 提交評論
        submitReviewButton.setOnClickListener {
            submitReview(roomID)
        }

        return view
    }

    private fun fetchToiletDetails(roomID: String) {
        database_restrooms.child(roomID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("buildingName").getValue(String::class.java) ?: "Unknown"
                    val roomNumber = snapshot.child("roomNumber").getValue(String::class.java) ?: "Unknown"
                    val address = snapshot.child("street").getValue(String::class.java) ?: "Unknown"
                    val rating = snapshot.child("averageOverallScore").getValue(Float::class.java) ?: 0f
                    val reviewsCount = snapshot.child("reviews_count").getValue(Long::class.java) ?: 0
//                    val recommendedPercentage = snapshot.child("recommended_percentage").getValue(Double::class.java) ?: 0.0

                    // 更新 UI
                    toiletName.text = name
                    toiletRoomNumber.text = roomNumber
                    toiletAddress.text = address
                    toiletRating.rating = rating
                    ratingStats.text = "$rating ★ | $reviewsCount Reviews"
//                    ratingStats.text = "$rating ★ | $reviewsCount Reviews | $recommendedPercentage% Recommended"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ToiletProfileFragment", "Error fetching data", error.toException())
            }
        })
    }

    private fun submitReview(roomID: String) {
        val title = reviewTitle.text.toString().trim()
        val body = reviewBody.text.toString().trim()
        val rating = toiletRating.rating // 獲取用戶選擇的評分
        val userID = "some_user_id" // 替換為實際用戶ID來源，例如 FirebaseAuth.getInstance().currentUser?.uid
        val imageURL = "some_image_url" // 如果有圖片，可以提供 URL；否則可為空字符串或不設置

        if (title.isEmpty() || body.isEmpty() || rating == 0.0f) {
            Toast.makeText(context, "Please fill in the title, review, and rating", Toast.LENGTH_SHORT).show()
            return
        }

        // 生成唯一的 reviewID
        val reviewID = database_restrooms.push().key ?: return

        // 構建評論的資料
        val review = mapOf(
            "reviewID" to reviewID,
            "roomID" to roomID,
            "userID" to userID,
            "reviewTitle" to title,
            "reviewBody" to body,
            "rating" to rating
        )

        // 將評論存到 Firebase 中的 reviews 表中

        database_reviews.child(reviewID).setValue(review)
            .addOnSuccessListener {
                Toast.makeText(context, "Review submitted successfully", Toast.LENGTH_SHORT).show()
                reviewTitle.text.clear()
                reviewBody.text.clear()
                toiletRating.rating = 0.0f // 重置評分為 0
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to submit review: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}