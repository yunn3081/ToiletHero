package com.example.myapplication.ui.notifications

import android.os.Bundle
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

class NotificationsFragment : Fragment() {

    // 初始化 FirebaseAuth
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)

        // 初始化 FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // 連接 UI 元素
        val emailEditText = view.findViewById<EditText>(R.id.email)
        val passwordEditText = view.findViewById<EditText>(R.id.password)
        val loginButton = view.findViewById<Button>(R.id.login_button)
        val signupButton = view.findViewById<Button>(R.id.signup_button)

        // 設置登入按鈕的點擊事件
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // 使用 FirebaseAuth 進行登入
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // 登入成功，導航到 AccountFragment
                            Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_notificationsFragment_to_accountFragment)
                        } else {
                            // 登入失敗，顯示錯誤訊息
                            Toast.makeText(
                                context,
                                "Login Failed: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        // 註冊按鈕的點擊事件
        signupButton.setOnClickListener {
            // 導航到 SignUpFragment
            findNavController().navigate(R.id.action_notificationsFragment_to_signUpFragment)
        }

        return view
    }
}
