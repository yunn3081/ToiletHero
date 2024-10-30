package com.example.myapplication.toilethero.review

data class Review(
    val reviewId: String = "",
    val roomId: String = "",
    val comment: String = "",
    val star: Float = 0f,
    val userId: String = ""
)
