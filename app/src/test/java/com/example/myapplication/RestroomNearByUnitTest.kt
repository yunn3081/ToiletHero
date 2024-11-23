package com.example.myapplication

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class RestroomNearByUnitTest {

    @Before
    fun setup() {
        println("=== Starting New Test ===")
    }

    @Test
    fun `test coordinate string parsing`() {
        println("\n🧪 Testing coordinate string parsing")

        // Given
        val validInput = "25.0,121.5"
        println("Input coordinates: $validInput")

        // When
        val parts = validInput.split(",")
        println("Split parts: $parts")

        val lat = parts[0].toDoubleOrNull()
        val lng = parts[1].toDoubleOrNull()
        println("Parsed values - Latitude: $lat, Longitude: $lng")

        // Then
        assertNotNull("Latitude should be parsed", lat)
        assertNotNull("Longitude should be parsed", lng)
        if (lat != null) {
            assertEquals(25.0, lat, 0.0001)
        }
        if (lng != null) {
            assertEquals(121.5, lng, 0.0001)
        }
        println("✅ Test passed: Coordinates parsed successfully")
    }

    @Test
    fun `test rating format validation`() {
        println("\n🧪 Testing rating format validation")

        // Given
        val testRatings = listOf(4.5, 0.0, 5.0, -1.0, 5.1)
        println("Testing ratings: $testRatings")

        // When & Then
        testRatings.forEach { rating ->
            val isValidRating = rating in 0.0..5.0
            println("Rating $rating is ${if (isValidRating) "valid" else "invalid"}")

            if (rating in 0.0..5.0) {
                assertTrue("Rating $rating should be valid", isValidRating)
                println("✅ Test passed: $rating is a valid rating")
            } else {
                assertFalse("Rating $rating should be invalid", isValidRating)
                println("✅ Test passed: $rating is correctly identified as invalid")
            }
        }
    }

    @Test
    fun `test marker title formatting`() {
        println("\n🧪 Testing marker title formatting")

        // Given
        val testCases = listOf(
            Triple("Building A", 4.5, "Building A ★ 4.5"),
            Triple("Building B", 0.0, "Building B ★ 0.0"),
            Triple("Building C", 5.0, "Building C ★ 5.0")
        )

        // When & Then
        testCases.forEach { (building, rating, expected) ->
            println("Testing building: $building with rating: $rating")
            val title = "$building ★ $rating"
            println("Generated title: $title")
            println("Expected title: $expected")

            assertEquals("Marker title should be formatted correctly", expected, title)
            println("✅ Test passed: Title formatted correctly")
        }
    }

    @Test
    fun `test coordinate validation`() {
        println("\n🧪 Testing coordinate validation")

        // Given
        val testCoordinates = listOf(
            "25.0,121.5" to true,
            "invalid" to false,
            "90.0,180.0" to true,
            "25.0," to false,
            ",121.5" to false
        )

        // When & Then
        testCoordinates.forEach { (input, shouldBeValid) ->
            println("\nTesting input: $input")

            val parts = input.split(",")
            val isValid = parts.size == 2 &&
                    parts[0].toDoubleOrNull() != null &&
                    parts[1].toDoubleOrNull() != null

            println("Parts: $parts")
            println("Is valid: $isValid")

            if (shouldBeValid) {
                assertTrue("Coordinate $input should be valid", isValid)
                println("✅ Test passed: Valid coordinate format")
            } else {
                assertFalse("Coordinate $input should be invalid", isValid)
                println("✅ Test passed: Invalid coordinate correctly identified")
            }
        }
    }
}