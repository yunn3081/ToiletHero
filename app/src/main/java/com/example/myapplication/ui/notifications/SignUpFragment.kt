package com.example.myapplication.ui.signup

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)

        // 初始化 FirebaseAuth 和 Firebase Realtime Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // 連接 UI 元素
        val emailEditText = view.findViewById<EditText>(R.id.email)
        val firstNameEditText = view.findViewById<EditText>(R.id.first_name)
        val lastNameEditText = view.findViewById<EditText>(R.id.last_name)
        val phoneEditText = view.findViewById<EditText>(R.id.phone_number)
        val dobEditText = view.findViewById<EditText>(R.id.date_of_birth)
        val signUpButton = view.findViewById<Button>(R.id.signup_button)

        // 註冊按鈕的點擊事件
        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val dob = dobEditText.text.toString().trim()
            val password = "defaultPassword123" // Firebase Auth requires a password

            // 簡單檢查是否輸入了 email 和 password
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Email and password are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 使用 FirebaseAuth 註冊帳戶
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // 获取当前用户的 UID
                        val userId = auth.currentUser?.uid

                        // 構建用戶對象
                        val user = User(userId, firstName, lastName, email, phone, dob)

                        // 将用户信息保存到Firebase Realtime Database
                        if (userId != null) {
                            database.child("users").child(userId).setValue(user)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // 保存成功后导航到 AccountFragment
                                        findNavController().navigate(R.id.action_signUpFragment_to_accountFragment)
                                        Toast.makeText(context, "Sign up successful!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        // 保存失敗，打印錯誤信息
                                        Log.e("DatabaseError", "Error: ${task.exception?.message}")
                                        Toast.makeText(
                                            context,
                                            "Failed to save user info: ${task.exception?.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }

                    } else {
                        // 註冊失敗，打印錯誤信息
                        Log.e("SignUpError", "Error: ${task.exception?.message}")
                        Toast.makeText(
                            context,
                            "Sign up failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        return view
    }

    // 用戶數據模型
    data class User(
        val userId: String?,
        val firstName: String,
        val lastName: String,
        val email: String,
        val phone: String,
        val dob: String
    )
}
