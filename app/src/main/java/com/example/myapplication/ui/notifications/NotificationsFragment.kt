package com.example.myapplication.ui.notifications
import SignUpFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R


class NotificationsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)

        // 获取EditText和Button
        val usernameEditText = view.findViewById<EditText>(R.id.username)
        val passwordEditText = view.findViewById<EditText>(R.id.password)
        val loginButton = view.findViewById<Button>(R.id.login_button)
        val signupButton = view.findViewById<Button>(R.id.signup_button)

        // 设置登录按钮点击事件
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            if (username.isNotEmpty() && password.isNotEmpty()) {
                Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Please enter username and password", Toast.LENGTH_SHORT).show()
            }
        }

        // 设置注册按钮点击事件
        signupButton.setOnClickListener {
            // 跳转到SignUpFragment
            findNavController().navigate(R.id.signUpFragment)
        }

        return view
    }


}
