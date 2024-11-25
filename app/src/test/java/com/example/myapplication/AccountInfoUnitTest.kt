package com.example.myapplication

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
//import kotlin.test.assertEquals
//import kotlin.test.assertNotNull
//import kotlin.test.assertTrue

class AccountInfoFragmentUnitTest {

    @Before
    fun setup() {
        println("=== Starting New Test ===")
    }

    @Test
    fun `test user info formatting`() {
        println("\n🧪 Testing user info formatting")

        // Given
        val userInfo = UserInfo("John", "Doe", "john.doe@example.com")
        println("Input UserInfo: $userInfo")

        // When
        val formattedInfo = formatUserInfo(userInfo)
        println("Formatted UserInfo: $formattedInfo")

        // Then
        assertEquals("John Doe (john.doe@example.com)", formattedInfo)
        println("✅ Test passed: User info formatted correctly")
    }

    @Test
    fun `test user email validation`() {
        println("\n🧪 Testing email validation")

        // Given
        val emails = listOf(
            "john.doe@example.com" to true,
            "invalid-email" to false,
            "test@domain" to false,
            "user.name+tag+sorting@example.com" to true
        )
        println("Testing emails: $emails")

        // When & Then
        emails.forEach { (email, expected) ->
            val isValid = validateEmail(email)
            println("Email: $email, Is valid: $isValid")

            if (expected) {
                assertTrue("Email $email should be valid", isValid)
                println("✅ Test passed: Valid email")
            } else {
                assertTrue("Email $email should be invalid", !isValid)
                println("✅ Test passed: Invalid email identified correctly")
            }
        }
    }

    @Test
    fun `test user first and last name validation`() {
        println("\n🧪 Testing user name validation")

        // Given
        val names = listOf(
            Pair("John", "Doe") to true,
            Pair("", "Smith") to false,
            Pair("Anna", "") to false,
            Pair("", "") to false
        )
        println("Testing names: $names")

        // When & Then
        names.forEach { (namePair, expected) ->
            val isValid = validateName(namePair.first, namePair.second)
            println("Names: ${namePair.first} ${namePair.second}, Is valid: $isValid")

            if (expected) {
                assertTrue("Name ${namePair.first} ${namePair.second} should be valid", isValid)
                println("✅ Test passed: Valid name")
            } else {
                assertTrue("Name ${namePair.first} ${namePair.second} should be invalid", !isValid)
                println("✅ Test passed: Invalid name identified correctly")
            }
        }
    }

    // Formatting logic
    private fun formatUserInfo(userInfo: UserInfo): String {
        return "${userInfo.firstName} ${userInfo.lastName} (${userInfo.email})"
    }

    // Email validation logic
    private fun validateEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }

    // Name validation logic
    private fun validateName(firstName: String, lastName: String): Boolean {
        return firstName.isNotEmpty() && lastName.isNotEmpty()
    }
}

data class UserInfo(
    val firstName: String,
    val lastName: String,
    val email: String
)
