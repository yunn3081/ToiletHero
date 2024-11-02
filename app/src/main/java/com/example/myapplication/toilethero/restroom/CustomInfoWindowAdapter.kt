package com.example.myapplication.toilethero.restroom

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.navigation.NavController
import com.example.myapplication.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoWindowAdapter(
    private val view: View,
    private val navController: NavController
) : GoogleMap.InfoWindowAdapter {

    override fun getInfoWindow(marker: Marker): View? {
        return null // Use default frame
    }

    override fun getInfoContents(marker: Marker): View {
        val buildingNameTextView: TextView = view.findViewById(R.id.building_name)
        val ratingTextView: TextView = view.findViewById(R.id.rating_text)
        val viewReviewsButton: Button = view.findViewById(R.id.view_reviews_button)

        // Set building name and rating text
        buildingNameTextView.text = marker.title
        ratingTextView.text = "★ 4.5" // Example rating; replace with actual rating as needed

        // Set up the button click listener to navigate to RestroomReviewFragment
        viewReviewsButton.setOnClickListener {
            navController.navigate(R.id.action_restroomNearbyFragment_to_restroomReviewFragment)
        }

        return view
    }
}
