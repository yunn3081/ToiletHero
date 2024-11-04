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
import androidx.fragment.app.activityViewModels
import com.example.myapplication.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RestroomNearbyFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: DatabaseReference
    private val viewModel: RestroomMapViewModel by activityViewModels()
    private var isDataLoaded = false // 用於跟踪 Firebase 數據是否已加載

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_restroom_nearby, container, false)

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Set up the map fragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize the Firebase Database reference
        database = FirebaseDatabase.getInstance().reference

        return view
    }

    override fun onResume() {
        super.onResume()
        // Apply full screen mode when the fragment is visible
        setFullScreenMode()
        view?.findViewById<View>(R.id.map_fragment)?.requestLayout()
    }

    override fun onPause() {
        super.onPause()
        if (this::map.isInitialized) {
            viewModel.cameraPosition = map.cameraPosition
        }
        // Cancel full screen mode when the fragment is no longer visible
        cancelFullScreenMode()
    }

    private fun setFullScreenMode() {
        activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }

    private fun cancelFullScreenMode() {
        activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
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

        // Restore camera position from ViewModel if available
        viewModel.cameraPosition?.let {
            map.moveCamera(CameraUpdateFactory.newCameraPosition(it))
        } ?: run {
            // Get current location and move the camera
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
                    viewModel.cameraPosition = map.cameraPosition
                }
            }
        }

        // Load restroom locations from Firebase only once
        if (!isDataLoaded) {
            loadRestroomsFromFirebase()
            isDataLoaded = true
        }

        // Set a click listener for markers
        map.setOnMarkerClickListener { marker ->
            val restroomId = marker.tag as? String
            restroomId?.let {
                val bottomSheet = RestroomDetailsBottomSheet.newInstance(it)
                bottomSheet.show(childFragmentManager, bottomSheet.tag)
            }
            true
        }

        // Save the camera position when the map moves
        map.setOnCameraIdleListener {
            viewModel.cameraPosition = map.cameraPosition
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
                                marker?.tag = restroomId
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

    private fun getBitmapDescriptorFromResource(resourceId: Int): BitmapDescriptor {
        val originalBitmap = BitmapFactory.decodeResource(resources, resourceId)
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 70, 70, false)
        return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    }
}
