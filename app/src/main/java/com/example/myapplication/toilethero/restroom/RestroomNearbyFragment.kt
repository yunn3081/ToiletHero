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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_restroom_nearby, container, false)

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        activity?.window?.decorView?.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )

        // Set up the map fragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize the Firebase Database reference
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
            // Request permissions if not granted
            return
        }
        map.isMyLocationEnabled = true

        // Get current location and move the camera
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
            }
        }

        // Load restroom locations from Firebase and display them
        loadRestroomsFromFirebase()

        // Set a click listener for markers
        map.setOnMarkerClickListener { marker ->
            // Open the RestroomDetailsBottomSheet when the marker's info window is clicked
            val restroomId = marker.tag as? String
            restroomId?.let {
                val bottomSheet = RestroomDetailsBottomSheet.newInstance(it)
                bottomSheet.show(childFragmentManager, bottomSheet.tag)
            }
            true
        }
    }

    private fun loadRestroomsFromFirebase() {
        database.child("restrooms").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (restroomSnapshot in snapshot.children) {
                    val gpsCoordinates = restroomSnapshot.child("gpsCoordinates").getValue(String::class.java)
                    val buildingName = restroomSnapshot.child("buildingName").getValue(String::class.java)
                    val rating = restroomSnapshot.child("rating").getValue(Double::class.java) ?: 0.0
                    val restroomId = restroomSnapshot.key

                    if (!gpsCoordinates.isNullOrBlank() && !buildingName.isNullOrBlank() && restroomId != null) {
                        // Split the coordinates into latitude and longitude
                        val coordinates = gpsCoordinates.split(",")
                        if (coordinates.size == 2) {
                            val latitude = coordinates[0].toDoubleOrNull()
                            val longitude = coordinates[1].toDoubleOrNull()

                            if (latitude != null && longitude != null) {
                                val restroomLocation = LatLng(latitude, longitude)
                                val icon = getBitmapDescriptorFromResource(R.drawable.restroom)

                                val marker = map.addMarker(
                                    MarkerOptions()
                                        .position(restroomLocation)
                                        .title("$buildingName ★ $rating")
                                        .icon(icon)
                                )
                                marker?.tag = restroomId // Store restroomId as the marker's tag
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
