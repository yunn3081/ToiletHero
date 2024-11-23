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
import com.google.firebase.database.*

class RestroomNearbyFragment : Fragment(), OnMapReadyCallback {

    lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var database: DatabaseReference
    val viewModel: RestroomMapViewModel by activityViewModels()
    var markers = mutableMapOf<String, Marker>() // Store markers by restroom ID

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
    open fun getVisibleBounds(): LatLngBounds? {
        return map?.projection?.visibleRegion?.latLngBounds
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_restroom_nearby, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        database = FirebaseDatabase.getInstance().reference.child("restrooms")

        return view
    }

    override fun onResume() {
        super.onResume()
        view?.findViewById<View>(R.id.map_fragment)?.requestLayout()
    }

    override fun onPause() {
        super.onPause()
        if (this::map.isInitialized) {
            viewModel.cameraPosition = map.cameraPosition
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isCompassEnabled = false
        map.uiSettings.isMapToolbarEnabled = false

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermissions()
            return
        }

        enableLocationOnMap()
    }

    private fun requestLocationPermissions() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableLocationOnMap()
        } else {
            Log.e("RestroomNearbyFragment", "Location permissions are required to use this feature.")
        }
    }

    fun enableLocationOnMap() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
            viewModel.cameraPosition?.let {
                map.moveCamera(CameraUpdateFactory.newCameraPosition(it))
            } ?: run {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val currentLatLng = LatLng(it.latitude, it.longitude)
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f))
                        viewModel.cameraPosition = map.cameraPosition
                    }
                }
            }

            map.setOnCameraIdleListener { loadRestroomsInVisibleRegion() }
            map.setOnMarkerClickListener { marker ->
                val restroomId = marker.tag as? String
                restroomId?.let {
                    val bottomSheet = RestroomDetailsBottomSheet.newInstance(it)
                    bottomSheet.show(childFragmentManager, bottomSheet.tag)
                }
                true
            }
        }
    }

    fun loadRestroomsInVisibleRegion() {
        val bounds = map.projection.visibleRegion.latLngBounds
        database.addListenerForSingleValueEvent(object : ValueEventListener {
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

                                if (bounds.contains(restroomLocation) && !markers.containsKey(restroomId)) {
                                    val icon = getBitmapDescriptorFromResource(R.drawable.restroom)
                                    val marker = map.addMarker(
                                        MarkerOptions()
                                            .position(restroomLocation)
                                            .title("$buildingName ★ $rating")
                                            .icon(icon)
                                    )
                                    marker?.tag = restroomId
                                    markers[restroomId] = marker!!
                                }
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
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 150, 150, false)
        return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    }
}
