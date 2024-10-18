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
    private val reviewList: MutableList<Review>, // MutableList 用來支持刪除
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

        // 初始進入頁面時不可編輯
        setEditable(holder, false)

        // 點擊 Edit 按鈕切換可編輯狀態
        holder.editButton.setOnClickListener {
            if (holder.isEditable) {
                val updatedComment = holder.commentEditText.text.toString()
                val updatedStar = holder.ratingBar.rating
                val updates = mapOf("comment" to updatedComment, "star" to updatedStar)

                database.child("reviews").child(currentUserId!!).child(review.reviewId).updateChildren(updates)
                setEditable(holder, false)
                holder.editButton.text = "Edit"
            } else {
                setEditable(holder, true)
                holder.editButton.text = "Save"
            }
        }

        // 點擊 Delete 按鈕刪除評論
        holder.deleteButton.setOnClickListener {
            database.child("reviews").child(currentUserId!!).child(review.reviewId).removeValue()
                .addOnSuccessListener {
                    // 確認 position 是否在列表範圍內
                    if (position >= 0 && position < reviewList.size) {
                        // 從本地列表中刪除評論
                        reviewList.removeAt(position)
                        // 通知 RecyclerView 該項目已刪除
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, reviewList.size)
                    }
                }
                .addOnFailureListener {
                    // 處理刪除失敗的情況
                    holder.deleteButton.text = "Delete Failed"
                }
        }
    }

    override fun getItemCount(): Int = reviewList.size

    private fun setEditable(holder: ReviewViewHolder, editable: Boolean) {
        holder.commentEditText.isEnabled = editable
        holder.roomIdEditText.isEnabled = editable
        holder.ratingBar.isEnabled = editable
        holder.isEditable = editable
    }

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val roomIdEditText: EditText = itemView.findViewById(R.id.room_id_text)
        val commentEditText: EditText = itemView.findViewById(R.id.comment_text)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        val editButton: Button = itemView.findViewById(R.id.edit_button)
        val deleteButton: Button = itemView.findViewById(R.id.delete_button)
        var isEditable: Boolean = false
    }
}
