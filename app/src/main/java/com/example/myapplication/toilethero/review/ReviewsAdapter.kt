package com.example.myapplication.toilethero.review

import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

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
//        holder.roomIdTextView.text = review.roomID.toString()
        holder.reviewTitleEditText.setText(review.reviewTitle)
        holder.reviewBodyEditText.setText(review.reviewBody)
        holder.ratingBar.rating = review.rating as Float

        fetchBuildingInfo(review.roomID.toString(), holder)


        if (isUserReviews) {
            // Hide edit and delete buttons, show More icon
            holder.roomNameTextView.visibility = View.VISIBLE
            holder.editButton.visibility = View.GONE
            holder.deleteButton.visibility = View.GONE
            holder.moreButton.visibility = View.VISIBLE

            // Set up More icon with PopupMenu
            holder.moreButton.setOnClickListener { view ->
                showPopupMenu(view, holder, position, review)
            }
        } else {
            // Hide all editing options in non-user mode
            holder.roomNameTextView.visibility = View.GONE
            holder.editButton.visibility = View.GONE
            holder.deleteButton.visibility = View.GONE
            holder.moreButton.visibility = View.GONE
            setEditable(holder, false)
        }
    }

    // Helper function to fetch building info based on roomID
    private fun fetchBuildingInfo(roomID: String, holder: ReviewViewHolder) {
        // Reference to the "restrooms" collection in Firebase
        val roomRef = database.child("restrooms").child(roomID)

        roomRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Get buildingName and roomNumber from snapshot
                    val buildingName = snapshot.child("buildingName").getValue(String::class.java) ?: "Unknown Building"
                    val roomNumber = snapshot.child("roomNumber").getValue(String::class.java) ?: "Unknown Room"

                    // Update ViewHolder with the retrieved data
                    holder.roomNameTextView.text = "$buildingName, Room $roomNumber"
                } else {
                    // Handle case where data doesn't exist for the given roomID
                    holder.roomNameTextView.text = "Room info not found"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                holder.roomNameTextView.text = "Failed to load room info"
                Log.e("ReviewsAdapter", "Error fetching room info: ${error.message}")
            }
        })
    }

    override fun getItemCount(): Int = reviewList.size

    private fun setEditable(holder: ReviewViewHolder, editable: Boolean) {
        holder.reviewTitleEditText.isEnabled = editable
        holder.reviewBodyEditText.isEnabled = editable
        holder.ratingBar.setIsIndicator(!editable) // Use setIsIndicator() to toggle interactivity
        holder.isEditable = editable
    }

    private fun showPopupMenu(view: View, holder: ReviewViewHolder, position: Int, review: Review) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(R.menu.review_options_menu) // A new XML menu file with Edit and Delete options

        val editMenuItem = popupMenu.menu.findItem(R.id.menu_edit)
        editMenuItem.title = if (holder.isEditable) "Save" else "Edit"

        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.menu_edit -> {
                    toggleEditMode(holder, review)
                    true
                }
                R.id.menu_delete -> {
                    deleteReview(position, review)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun toggleEditMode(holder: ReviewViewHolder, review: Review) {
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
            setEditable(holder, false) // Disable edit mode after saving
        } else {
            setEditable(holder, true) // Enable edit mode
        }
    }

    private fun deleteReview(position: Int, review: Review) {
        currentUserId?.let {
            database.child("reviews").child(it).child(review.reviewID.toString()).removeValue()
                .addOnSuccessListener {
                    if (position >= 0 && position < reviewList.size) {
                        reviewList.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, reviewList.size)
                    }
                }
        }
    }

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val roomNameTextView: TextView = itemView.findViewById(R.id.room_name_text)
        val reviewTitleEditText: EditText = itemView.findViewById(R.id.review_title_text)
        val reviewBodyEditText: EditText = itemView.findViewById(R.id.review_body_text)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        val editButton: Button = itemView.findViewById(R.id.edit_button)
        val deleteButton: Button = itemView.findViewById(R.id.delete_button)
        val moreButton: ImageView = itemView.findViewById(R.id.more_button) // New More button
        var isEditable: Boolean = false
    }
}
