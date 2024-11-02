package com.example.myapplication.toilethero.restroom

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.*

class RestroomNearbyFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: DatabaseReference
    private var selectedMarker: Marker? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_restroom_nearby, container, false)

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Set up map fragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        map.isMyLocationEnabled = true

        // Center the camera on the user location
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
            }
        }

        // Load restroom locations from Firebase
        loadRestroomsFromFirebase()

        // Set a custom InfoWindowAdapter
        map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? {
                return null // Use default frame for the info window
            }

            override fun getInfoContents(marker: Marker): View {
                val infoView = layoutInflater.inflate(R.layout.custom_info_window, null)

                val buildingNameTextView = infoView.findViewById<TextView>(R.id.building_name)
                val ratingTextView = infoView.findViewById<TextView>(R.id.rating_text)
                val viewReviewsButton = infoView.findViewById<Button>(R.id.view_reviews_button)

                buildingNameTextView.text = marker.title
                ratingTextView.text = "4.5 ★" // Example rating; replace with real data if available

                // Set up the button click listener
                viewReviewsButton.setOnClickListener {
                    // Navigate to RestroomReviewFragment when the button is clicked
                    findNavController().navigate(R.id.action_restroomNearbyFragment_to_restroomReviewFragment)
                }

                return infoView
            }
        })

        // Set a click listener for markers
        map.setOnMarkerClickListener { marker ->
            selectedMarker = marker
            marker.showInfoWindow()
            true
        }
    }

    private fun loadRestroomsFromFirebase() {
        database.child("restrooms").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (restroomSnapshot in snapshot.children) {
                    val gpsCoordinates = restroomSnapshot.child("gpsCoordinates").getValue(String::class.java)
                    val buildingName = restroomSnapshot.child("buildingName").getValue(String::class.java)

                    if (!gpsCoordinates.isNullOrBlank() && !buildingName.isNullOrBlank()) {
                        val coordinates = gpsCoordinates.split(",")
                        if (coordinates.size == 2) {
                            val latitude = coordinates[0].toDoubleOrNull()
                            val longitude = coordinates[1].toDoubleOrNull()

                            if (latitude != null && longitude != null) {
                                val restroomLocation = LatLng(latitude, longitude)
                                val icon = getBitmapDescriptorFromResource(R.drawable.restroom)

                                map.addMarker(
                                    MarkerOptions()
                                        .position(restroomLocation)
                                        .title(buildingName)
                                        .icon(icon)
                                )
                            }
                        }
                    } else {
                        Log.e("RestroomNearbyFragment", "Missing gpsCoordinates or buildingName")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RestroomNearbyFragment", "Failed to load restrooms: ${error.message}")
            }
        })
    }

    // Helper function to get a resized BitmapDescriptor from a resource
    private fun getBitmapDescriptorFromResource(resourceId: Int): BitmapDescriptor {
        val originalBitmap = BitmapFactory.decodeResource(resources, resourceId)
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 70, 70, false)
        return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    }
}
