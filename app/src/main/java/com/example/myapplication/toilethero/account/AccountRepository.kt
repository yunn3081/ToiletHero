package com.example.myapplication.toilethero.account

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import kotlinx.coroutines.tasks.await

open class AccountRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
) {

    open suspend fun getUserData(): Map<String, String>? {
        val userId = auth.currentUser?.uid ?: return null
        val snapshot = database.child(userId).get().await() // 使用协程处理异步调用
        return if (snapshot.exists()) {
            mapOf(
                "firstName" to snapshot.child("firstName").getValue(String::class.java).orEmpty(),
                "lastName" to snapshot.child("lastName").getValue(String::class.java).orEmpty(),
                "email" to snapshot.child("email").getValue(String::class.java).orEmpty(),
                "phone" to snapshot.child("phone").getValue(String::class.java).orEmpty(),
                "dob" to snapshot.child("dob").getValue(String::class.java).orEmpty()
            )
        } else null
    }

    open suspend fun updateUserData(data: Map<String, String>): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        return try {
            database.child(userId).updateChildren(data).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    open fun signOut() {
        auth.signOut()
    }
}
