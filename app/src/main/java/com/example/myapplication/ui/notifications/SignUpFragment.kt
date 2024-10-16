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
import com.google.firebase.firestore.FirebaseFirestore

class SignUpFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)

        // 初始化 FirebaseAuth 和 Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 連接 UI 元素
        val emailEditText = view.findViewById<EditText>(R.id.email)
        val signUpButton = view.findViewById<Button>(R.id.signup_button)

        // 註冊按鈕的點擊事件
        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
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
                        // 註冊成功後導航到 AccountFragment
                        findNavController().navigate(R.id.action_signUpFragment_to_accountFragment)
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
}
