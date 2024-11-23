package com.example.myapplication.toilethero.toiletProfile

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.myapplication.R
import com.google.firebase.database.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import com.example.myapplication.BuildConfig
import com.example.myapplication.toilethero.review.Review
import com.example.myapplication.toilethero.review.ReviewsAdapter
import com.google.firebase.auth.FirebaseAuth
import okio.IOException

class ToiletProfileFragment : Fragment() {

    private lateinit var toiletName: TextView
    private lateinit var toiletRoomNumber: TextView
    private lateinit var toiletAddress: TextView
    private lateinit var toiletRating: RatingBar
    private lateinit var ratingStats: TextView
    private lateinit var reviewTitle: EditText
    private lateinit var reviewBody: EditText
    private lateinit var submitReviewButton: Button
    private lateinit var loginButton: Button
    private lateinit var restroomImageView: ImageView
    private lateinit var loadingSpinner: ProgressBar
    private lateinit var database_restrooms: DatabaseReference
    private lateinit var database_reviews: DatabaseReference
    private val handler = Handler(Looper.getMainLooper())
    private val timeoutDuration = 10000L // 10 seconds
    private val apiKey = BuildConfig.GOOGLE_MAPS_API_KEY
    private val auth = FirebaseAuth.getInstance()
    private lateinit var reviewsRecyclerView: RecyclerView
    private lateinit var reviewsAdapter: ReviewsAdapter
    private var reviewList = mutableListOf<Review>()
    private lateinit var noReviewsTextView: TextView
    private lateinit var directionIcon: ImageView
    private var gpsCoordinates: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_toilet_profile, container, false)

        toiletName = view.findViewById(R.id.toilet_name)
        toiletRoomNumber = view.findViewById(R.id.toilet_roomNumber)
        toiletAddress = view.findViewById(R.id.toilet_address)
        toiletRating = view.findViewById(R.id.toilet_rating)
        ratingStats = view.findViewById(R.id.rating_stats)
        reviewTitle = view.findViewById(R.id.review_title)
        reviewBody = view.findViewById(R.id.review_body)
        submitReviewButton = view.findViewById(R.id.submit_review_button)
        loginButton = view.findViewById(R.id.login_button)
        restroomImageView = view.findViewById(R.id.restroom_image)
        loadingSpinner = view.findViewById(R.id.loadingSpinner)
        noReviewsTextView = view.findViewById(R.id.no_reviews_text_view)
        directionIcon = view.findViewById(R.id.direction_icon) // 導航圖標

        val roomID = arguments?.getString("roomID") ?: return view

        restroomImageView.visibility = View.INVISIBLE
        loadingSpinner.visibility = View.VISIBLE

        database_restrooms = FirebaseDatabase.getInstance().getReference("restrooms")
        database_reviews = FirebaseDatabase.getInstance().getReference("reviews")

        // Initialize Firebase Database reference for reviews
        database_reviews = FirebaseDatabase.getInstance().reference.child("reviews")

        // Setup RecyclerView and attach the adapter
        reviewsRecyclerView = view.findViewById(R.id.reviews_recycler_view)
        reviewsRecyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize adapter and set to RecyclerView
        reviewsAdapter = ReviewsAdapter(reviewList, database_reviews, null, false)
        reviewsRecyclerView.layoutManager = LinearLayoutManager(context)
        reviewsRecyclerView.adapter = reviewsAdapter

        // Load reviews into the list
        loadReviews(roomID)

        // Fetch toilet details and check authentication status
        fetchToiletDetails(roomID)
        checkAuthStatus()

        submitReviewButton.setOnClickListener {
            submitReview(roomID)
        }

        loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_toiletProfileFragment_to_loginFragment, Bundle().apply {
                putBoolean("returnToReviewPage", true)
            })
//            findNavController().navigate(R.id.action_toiletProfileFragment_to_loginFragment)

        }

        Log.d("ToiletProfileFragment", "Fetched GPS Coordinate: $gpsCoordinates")

        directionIcon.setOnClickListener {
            gpsCoordinates?.let { coordinates ->
                // 打印出 gpsCoordinates
                Log.d("ToiletProfileFragment", "GPS Coordinates: $coordinates")
                openGoogleMapsNavigation(coordinates)
            } ?: Toast.makeText(context, "GPS coordinates not available", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun fetchToiletDetails(roomID: String) {
        database_restrooms.child(roomID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                activity?.runOnUiThread {
                    if (snapshot.exists()) {
                        val name = snapshot.child("buildingName").getValue(String::class.java) ?: "Unknown"
                        val roomNumber = snapshot.child("roomNumber").getValue(String::class.java) ?: "Unknown"
                        val address = snapshot.child("street").getValue(String::class.java) ?: "Unknown"
                        val rating = snapshot.child("averageOverallScore").getValue(Float::class.java) ?: 0f
                        val reviewsCount = snapshot.child("reviewsCount").getValue(Long::class.java) ?: 0
                        gpsCoordinates = snapshot.child("gpsCoordinates").getValue(String::class.java) // 賦值給成員變數

                        // 更新 UI
                        toiletName.text = name
                        toiletRoomNumber.text = roomNumber
                        toiletAddress.text = address
                        toiletRating.rating = rating
                        ratingStats.text = "$rating ★ | $reviewsCount Reviews"
                        ratingStats.text = ratingStats.context.getString(
                            R.string.rating_stats,
                            rating ?: 0.0,
                            reviewsCount ?: 0
                        )

                        gpsCoordinates?.let {
                            Log.d("ToiletProfileFragment", "Fetched GPS Coordinates: $gpsCoordinates")
                            val (latitude, longitude) = it.split(",").map { coord -> coord.trim().toDouble() }
                            fetchPlaceId(latitude, longitude) { placeIds ->
                                placeIds?.let {
                                    fetchPhotosSequentially(placeIds, restroomImageView, loadingSpinner)
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ToiletProfileFragment", "Error fetching data", error.toException())
            }
        })
    }


    private fun openGoogleMapsNavigation(coordinates: String) {
        val (latitude, longitude) = coordinates.split(",").map { it.trim() }
        val uri = "google.navigation:q=$latitude,$longitude"
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri))
        intent.setPackage("com.google.android.apps.maps")
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(context, "Google Maps app is not installed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchPlaceId(latitude: Double, longitude: Double, callback: (List<String>?) -> Unit) {
        val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=$latitude,$longitude&rankby=distance&key=$apiKey"
        val client = OkHttpClient()

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

    private fun fetchPhotoReference(placeId: String, callback: (String?) -> Unit) {
        val url = "https://maps.googleapis.com/maps/api/place/details/json?place_id=$placeId&key=$apiKey"
        val client = OkHttpClient()

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
            Glide.with(this)
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

    private fun submitReview(roomID: String) {
        val title = reviewTitle.text.toString().trim()
        val body = reviewBody.text.toString().trim()
        val rating = toiletRating.rating
        val userID = FirebaseAuth.getInstance().currentUser?.uid

        if (title.isEmpty() || body.isEmpty() || rating == 0.0f) {
            Toast.makeText(context, "Please fill in the title, review, and rating", Toast.LENGTH_SHORT).show()
            return
        }

        // 從 Firebase 中讀取當前的 `averageOverallScore` 和 `reviewsCount`
        database_restrooms.child(roomID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val currentScore = snapshot.child("averageOverallScore").getValue(Float::class.java) ?: 0f
//                    val reviewsCount = snapshot.child("reviewsCount").getValue(Long::class.java) ?: 0

                    val actualReviewCount = snapshot.childrenCount // 獲取實際評論數量
                    // 計算新的 `averageOverallScore` 和增加 `reviewsCount`
                    val updatedCount = actualReviewCount + 1
                    val updatedScore = ((currentScore * actualReviewCount) + rating) / updatedCount

                    // 構建評論和更新的數據
                    val reviewID = database_reviews.child(roomID).push().key ?: return
                    val review = mapOf(
                        "userID" to userID,
                        "reviewTitle" to title,
                        "reviewBody" to body,
                        "rating" to rating
                    )

                    val updates = mapOf(
                        "reviews/$roomID/$reviewID" to review,
                        "restrooms/$roomID/averageOverallScore" to updatedScore,
                        "restrooms/$roomID/reviewsCount" to updatedCount
                    )

                    // 將評論和更新後的 `averageOverallScore` 及 `reviewsCount` 一起寫入 Firebase
                    FirebaseDatabase.getInstance().reference.updateChildren(updates)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Review submitted successfully", Toast.LENGTH_SHORT).show()
                            reviewTitle.text.clear()
                            reviewBody.text.clear()
                            toiletRating.rating = 0.0f // 重置評分為 0
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Failed to submit review: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ToiletProfileFragment", "Error updating average score", error.toException())
            }
        })
    }

    // Check if user is logged in to show/hide the appropriate buttons
    private fun checkAuthStatus() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            println("User is logged in: ${user.email ?: "Anonymous"}")
            submitReviewButton.visibility = View.VISIBLE
            reviewTitle.visibility = View.VISIBLE
            reviewBody.visibility = View.VISIBLE
            ratingStats.visibility = View.VISIBLE
            toiletRating.visibility = View.VISIBLE
            loginButton.visibility = View.GONE
        } else {
            println("User is not logged in")
            submitReviewButton.visibility = View.GONE
            reviewTitle.visibility = View.GONE
            reviewBody.visibility = View.GONE
            ratingStats.visibility = View.GONE
            toiletRating.visibility = View.GONE
            loginButton.visibility = View.VISIBLE
        }
    }
//    private fun loadRestroomReviews(roomID: String) {
//        database_reviews.orderByChild("roomID").equalTo(roomID)
//            .addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    reviewList.clear()
//                    for (reviewSnapshot in snapshot.children) {
//                        val review = reviewSnapshot.getValue(Review::class.java)
//                        review?.let { reviewList.add(it) }
//                    }
//                    reviewsAdapter.notifyDataSetChanged()
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Log.e("ToiletProfileFragment", "Error loading reviews", error.toException())
//                }
//            })
//    }
private fun loadReviews(roomID: String) {
    database_reviews.child(roomID)
        .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                reviewList.clear()
                if (snapshot.exists()) {
                    noReviewsTextView.visibility = View.GONE
                    for (reviewSnapshot in snapshot.children) {
                        val reviewMap = reviewSnapshot.value as Map<*, *>
                        val ratingValue = (reviewMap["rating"] as? Number)?.toFloat() ?: 0.0f
                        val review = Review(
                            reviewID = reviewSnapshot.key ?: "",
                            roomID = reviewMap["roomID"] as? String ?: "",
                            userID = reviewMap["userID"] as? String ?: "",
                            reviewTitle = reviewMap["reviewTitle"] as? String ?: "",
                            reviewBody = reviewMap["reviewBody"] as? String ?: "",
                            rating = ratingValue
                        )
                        reviewList.add(review)
                    }
                } else {
                    noReviewsTextView.visibility = View.VISIBLE
                }
                reviewsAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load reviews", Toast.LENGTH_SHORT).show()
            }
        })
}


}
