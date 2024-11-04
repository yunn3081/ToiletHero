package com.example.myapplication.toilethero.restroom

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.CameraPosition

class RestroomMapViewModel : ViewModel() {
    var cameraPosition: CameraPosition? = null
    var isMapInitialized: Boolean = false
}
