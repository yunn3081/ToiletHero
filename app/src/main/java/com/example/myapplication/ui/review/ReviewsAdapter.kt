package com.example.myapplication.ui.review

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.google.firebase.database.DatabaseReference

class ReviewsAdapter(
    private val reviewList: List<Review>,
    private val database: DatabaseReference,
    private val currentUserId: String?
) : RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]

        holder.roomIdEditText.setText(review.roomId)
        holder.commentEditText.setText(review.comment)
        holder.ratingBar.rating = review.star

        if (review.userId == currentUserId) {
            holder.editButton.visibility = View.VISIBLE
            holder.deleteButton.visibility = View.VISIBLE

            // 編輯評論
            holder.editButton.setOnClickListener {
                val updatedComment = holder.commentEditText.text.toString()
                val updatedStar = holder.ratingBar.rating
                val updates = mapOf("comment" to updatedComment, "star" to updatedStar)

                database.child("reviews").child(currentUserId!!).child(review.reviewId).updateChildren(updates)
            }

            // 刪除評論
            holder.deleteButton.setOnClickListener {
                database.child("reviews").child(currentUserId!!).child(review.reviewId).removeValue()
            }
        }
    }

    override fun getItemCount(): Int = reviewList.size

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val roomIdEditText: EditText = itemView.findViewById(R.id.room_id_text)
        val commentEditText: EditText = itemView.findViewById(R.id.comment_text)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        val editButton: Button = itemView.findViewById(R.id.edit_button)
        val deleteButton: Button = itemView.findViewById(R.id.delete_button)
    }
}
