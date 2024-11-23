package com.example.myapplication
 object RestRoomMockData {
        val restRoomList = listOf(
            RestRoom(
                name = "Building A Restroom",
                latitude = 25.123456,
                longitude = 121.654321,
                rating = 4.5
            ),
            RestRoom(
                name = "City Park Restroom",
                latitude = 25.987654,
                longitude = 121.123456,
                rating = 3.8
            ),
            RestRoom(
                name = "Shopping Mall Restroom",
                latitude = 25.456789,
                longitude = 121.987654,
                rating = 4.2
            )
        )
    }

    data class RestRoom(
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val rating: Double
    )
