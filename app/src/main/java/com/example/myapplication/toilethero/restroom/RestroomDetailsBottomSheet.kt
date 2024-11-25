package com.example.myapplication.toilethero.restroom

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.example.myapplication.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.json.JSONObject
import com.example.myapplication.BuildConfig

class RestroomDetailsBottomSheet : BottomSheetDialogFragment() {

    private var restroomId: String? = null
    private lateinit var database: DatabaseReference
    private val apiKey = BuildConfig.GOOGLE_MAPS_API_KEY
    private val handler = Handler(Looper.getMainLooper())
    private val timeoutDuration = 15000L // 10 seconds
    private val client = OkHttpClient()

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
        database = FirebaseDatabase.getInstance().reference
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
        val reviewsTextView = view.findViewById<TextView>(R.id.reviews_text)
        val loadingSpinner = view.findViewById<ProgressBar>(R.id.loadingSpinner)

        // Initially hide the ImageView and show the loading spinner
        restroomImageView.visibility = View.INVISIBLE
        loadingSpinner.visibility = View.VISIBLE

        restroomId?.let { id ->
            database.child("restrooms").child(id).addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("StringFormatMatches")
                override fun onDataChange(snapshot: DataSnapshot) {
                    val buildingName = snapshot.child("buildingName").getValue(String::class.java)
                    val address = snapshot.child("street").getValue(String::class.java)
                    val rating = snapshot.child("averageOverallScore").getValue(Double::class.java)
                    val gpsCoordinates = snapshot.child("gpsCoordinates").getValue(String::class.java)

                    buildingNameTextView.text = buildingName ?: "No Name"
                    restroomAddressTextView.text = address ?: "No Address"
                    ratingTextView.text = ratingTextView.context.getString(R.string.rating_text, rating ?: 0.0)


                    gpsCoordinates?.let {
                        val (latitude, longitude) = it.split(",").map { coord -> coord.trim().toDouble() }
                        fetchPlaceId(latitude, longitude) { placeIds ->
                            placeIds?.let {
                                fetchPhotosSequentially(placeIds, restroomImageView, loadingSpinner)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        reviewsTextView.setOnClickListener {
            restroomId?.let { id ->
                val bundle = Bundle().apply {
                    putString("roomID", id)
                }
                try {
                    findNavController().navigate(R.id.toiletProfileFragment, bundle)
                } catch (e: IllegalArgumentException) {
                    Toast.makeText(context, "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Set a timeout to show the default image if no photo is found within 10 seconds
        handler.postDelayed({
            loadingSpinner.visibility = View.GONE
            restroomImageView.setImageResource(R.drawable.noimage)
            restroomImageView.visibility = View.VISIBLE // Show the ImageView with the default image
        }, timeoutDuration)

        return view
    }

    fun fetchPlaceId(latitude: Double, longitude: Double, callback: (List<String>?) -> Unit) {
        val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=$latitude,$longitude&rankby=distance&key=$apiKey"
//        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val jsonResponse = response.body?.string()
                    val jsonObject = JSONObject(jsonResponse ?: "")
                    val resultsArray = jsonObject.optJSONArray("results")
                    val placeIds = mutableListOf<String>()
                    if (resultsArray != null && resultsArray.length() > 0) {
                        for (i in 0 until resultsArray.length()) {
                            val placeId = resultsArray.getJSONObject(i).getString("place_id")
                            placeIds.add(placeId)
                        }
                        callback(placeIds)
                    } else {
                        callback(null)
                    }
                } else {
                    callback(null)
                }
            }
        })
    }

    private fun fetchPhotosSequentially(placeIds: List<String>, imageView: ImageView, loadingSpinner: ProgressBar, index: Int = 0) {
        if (index >= placeIds.size) {
            handler.removeCallbacksAndMessages(null)
            loadingSpinner.visibility = View.GONE
            imageView.setImageResource(R.drawable.noimage)
            imageView.visibility = View.VISIBLE
            return
        }

        fetchPhotoReference(placeIds[index]) { photoReference ->
            if (photoReference != null) {
                loadPlaceImage(photoReference, imageView, loadingSpinner)
            } else {
                fetchPhotosSequentially(placeIds, imageView, loadingSpinner, index + 1)
            }
        }
    }

    fun fetchPhotoReference(placeId: String, callback: (String?) -> Unit) {
        val url = "https://maps.googleapis.com/maps/api/place/details/json?place_id=$placeId&key=$apiKey"
//        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val jsonResponse = response.body?.string()
                    val jsonObject = JSONObject(jsonResponse ?: "")
                    val result = jsonObject.optJSONObject("result")
                    val photosArray = result?.optJSONArray("photos")
                    if (photosArray != null && photosArray.length() > 0) {
                        val photoReference = photosArray.getJSONObject(0).getString("photo_reference")
                        callback(photoReference)
                    } else {
                        callback(null)
                    }
                } else {
                    callback(null)
                }
            }
        })
    }

    private fun loadPlaceImage(photoReference: String, imageView: ImageView, loadingSpinner: ProgressBar) {
        val url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=$photoReference&key=$apiKey"

        activity?.runOnUiThread {
            Glide.with(this@RestroomDetailsBottomSheet)
                .load(url)
                .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        handler.removeCallbacksAndMessages(null)
                        loadingSpinner.visibility = View.GONE
                        imageView.setImageResource(R.drawable.noimage)
                        imageView.visibility = View.VISIBLE
                        return false
                    }

                    override fun onResourceReady(
                        resource: android.graphics.drawable.Drawable?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                        dataSource: com.bumptech.glide.load.DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        handler.removeCallbacksAndMessages(null)
                        loadingSpinner.visibility = View.GONE
                        imageView.visibility = View.VISIBLE
                        return false
                    }
                })
                .into(imageView)
        }
    }
}
