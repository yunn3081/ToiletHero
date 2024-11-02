package com.example.myapplication.toilethero.restroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class RestroomDetailsBottomSheet : BottomSheetDialogFragment() {

    private var restroomId: String? = null
    private lateinit var database: DatabaseReference

    companion object {
        fun newInstance(restroomId: String): RestroomDetailsBottomSheet {
            val args = Bundle()
            args.putString("restroomId", restroomId)
            val fragment = RestroomDetailsBottomSheet()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restroomId = arguments?.getString("restroomId")
        database = FirebaseDatabase.getInstance().reference // Initialize Firebase reference
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_restroom_details_bottom_sheet, container, false)

        val buildingNameTextView = view.findViewById<TextView>(R.id.restroom_name)
        val restroomAddressTextView = view.findViewById<TextView>(R.id.restroom_address)
        val ratingTextView = view.findViewById<TextView>(R.id.rating_reviews_header)
        val restroomImageView = view.findViewById<ImageView>(R.id.restroom_image)

        // Load restroom details from Firebase using restroomId
        restroomId?.let { id ->
            database.child("restrooms").child(id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val buildingName = snapshot.child("buildingName").getValue(String::class.java)
                    val address = snapshot.child("street").getValue(String::class.java)
                    val rating = snapshot.child("rating").getValue(Double::class.java)
                    val imageUrl = snapshot.child("imageUrl").getValue(String::class.java)

                    buildingNameTextView.text = buildingName ?: "No Name"
                    restroomAddressTextView.text = address ?: "No Address"
                    ratingTextView.text = "${rating ?: 0.0} ★"

                    // Load image using Glide (make sure to add Glide dependency in your build.gradle)
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this@RestroomDetailsBottomSheet)
                            .load(imageUrl)
                            .into(restroomImageView)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the error, e.g., show a toast or log the error
                }
            })
        }

        return view
    }
}
