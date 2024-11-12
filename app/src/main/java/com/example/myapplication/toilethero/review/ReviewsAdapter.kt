package com.example.myapplication.toilethero.review

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.google.firebase.database.DatabaseReference

class ReviewsAdapter(
    private val reviewList: MutableList<Review>,
    private val database: DatabaseReference,
    private val currentUserId: String?,
    private val isUserReviews: Boolean // Flag to determine if this is for the account or toilet details
) : RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]

        // Display data in the ViewHolder
        holder.roomIdTextView.text = review.roomID.toString()
        holder.reviewTitleEditText.setText(review.reviewTitle)
        holder.reviewBodyEditText.setText(review.reviewBody)
        holder.ratingBar.rating = review.rating as Float

        if (isUserReviews) {
            // Enable edit and delete buttons for user's own reviews
            setEditable(holder, false)
            holder.editButton.visibility = View.VISIBLE
            holder.deleteButton.visibility = View.VISIBLE

            holder.editButton.setOnClickListener {
                if (holder.isEditable) {
                    val updatedTitle = holder.reviewTitleEditText.text.toString()
                    val updatedBody = holder.reviewBodyEditText.text.toString()
                    val updatedRating = holder.ratingBar.rating

                    val updates = mapOf(
                        "reviewTitle" to updatedTitle,
                        "reviewBody" to updatedBody,
                        "rating" to updatedRating
                    )

                    // Update the review in Firebase
                    currentUserId?.let {
                        database.child("reviews").child(it).child(review.reviewID.toString()).updateChildren(updates)
                    }
                    setEditable(holder, false)
                    holder.editButton.text = "Edit"
                } else {
                    setEditable(holder, true)
                    holder.editButton.text = "Save"
                }
            }

            holder.deleteButton.setOnClickListener {
                currentUserId?.let {
                    database.child("reviews").child(it).child(review.reviewID.toString()).removeValue()
                        .addOnSuccessListener {
                            if (position >= 0 && position < reviewList.size) {
                                reviewList.removeAt(position)
                                notifyItemRemoved(position)
                                notifyItemRangeChanged(position, reviewList.size)
                            }
                        }
                        .addOnFailureListener {
                            holder.deleteButton.text = "Delete Failed"
                        }
                }
            }
        } else {
            // Disable edit and delete buttons in non-user view mode
            holder.editButton.visibility = View.GONE
            holder.deleteButton.visibility = View.GONE
            holder.roomIdTextView.visibility = View.GONE
            setEditable(holder, false)
        }
    }

    override fun getItemCount(): Int = reviewList.size

    private fun setEditable(holder: ReviewViewHolder, editable: Boolean) {
        holder.reviewTitleEditText.isEnabled = editable
        holder.reviewBodyEditText.isEnabled = editable
        holder.ratingBar.isEnabled = editable
        holder.isEditable = editable
    }

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val roomIdTextView: TextView = itemView.findViewById(R.id.room_id_text)
        val reviewTitleEditText: EditText = itemView.findViewById(R.id.review_title_text)
        val reviewBodyEditText: EditText = itemView.findViewById(R.id.review_body_text)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        val editButton: Button = itemView.findViewById(R.id.edit_button)
        val deleteButton: Button = itemView.findViewById(R.id.delete_button)
        var isEditable: Boolean = false
    }
}
