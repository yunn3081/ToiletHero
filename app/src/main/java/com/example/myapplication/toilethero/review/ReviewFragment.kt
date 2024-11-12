package com.example.myapplication.toilethero.review

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ReviewFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var reviewAdapter: ReviewsAdapter
    private var reviewList = mutableListOf<Review>()
    private lateinit var noReviewsTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_review, container, false)

        // 初始化 FirebaseAuth 和 Firebase Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // 連接 UI 元素
        val createReviewButton = view.findViewById<Button>(R.id.create_review_button)
        noReviewsTextView = view.findViewById(R.id.no_reviews_text_view)
        val recyclerView = view.findViewById<RecyclerView>(R.id.reviews_recycler_view)

        // 設置 RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        reviewAdapter = ReviewsAdapter(reviewList, database, auth.currentUser?.uid, true)
        recyclerView.adapter = reviewAdapter

        // 移除 ItemDecoration，這樣評論之間不會有額外的間距
        // recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
        //     override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        //         outRect.bottom = 8  // 刪除這個間距
        //     }
        // })

        // 設置創建評論按鈕
        createReviewButton.setOnClickListener {
            findNavController().navigate(R.id.action_reviewFragment_to_createReviewFragment)
        }

        // 讀取用戶評論
        fetchUserReviews()
//        loadReviews()

        return view
    }

//    private fun loadReviews() {
//        val userId = auth.currentUser?.uid ?: return
//        database.child("reviews").child(userId).addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                reviewList.clear()
//                if (snapshot.exists()) {
//                    noReviewsTextView.visibility = View.GONE // 有評論時隱藏 "No reviews"
//                    for (reviewSnapshot in snapshot.children) {
//                        val review = reviewSnapshot.getValue(Review::class.java)
//                        review?.let { reviewList.add(it) }
//                    }
//                } else {
//                    noReviewsTextView.visibility = View.VISIBLE // 沒有評論時顯示 "No reviews"
//                }
//                reviewAdapter.notifyDataSetChanged()
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.e("ReviewFragment", "Failed to load reviews", error.toException())
//            }
//        })

    // Example of fetching and filtering reviews (inefficient for large datasets)
    private fun fetchUserReviews() {
        val userID = auth.currentUser?.uid ?: return
        val reviewsRef = database.child("reviews")

        reviewsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userReviews = mutableListOf<Review>()

                for (roomSnapshot in snapshot.children) {
                    val roomID = roomSnapshot.key?.toString() ?: continue

                    for (reviewSnapshot in roomSnapshot.children) {
                        try {
                            val rawReview = reviewSnapshot.value as? Map<*, *>

                            // Extract and convert each field explicitly
                            val review = Review(
                                reviewID = rawReview?.get("reviewID")?.toString(),
                                roomID = roomID,
                                userID = rawReview?.get("userID")?.toString(),
                                reviewTitle = rawReview?.get("reviewTitle") as? String ?: "",
                                reviewBody = rawReview?.get("reviewBody") as? String ?: "",
                                rating = (rawReview?.get("rating") as? Number)?.toFloat() ?: 0.0f
                            )

                            if (review.userID == userID) {
                                userReviews.add(review)
                            }
                        } catch (e: Exception) {
                            Log.e("ReviewFetch", "Error converting review: ${e.message}")
                        }
                    }
                }

                // Update RecyclerView
                reviewList.clear()
                reviewList.addAll(userReviews)
                reviewAdapter.notifyDataSetChanged()

                // Show or hide "No reviews" text
                noReviewsTextView.visibility = if (userReviews.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ReviewFetch", "Error fetching reviews: ${error.message}")
            }
        })
    }


}