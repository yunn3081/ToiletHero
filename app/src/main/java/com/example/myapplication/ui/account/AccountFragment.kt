package com.example.myapplication.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth

class AccountFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        // 初始化 FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // 連接 UI 元素
        val logoutButton = view.findViewById<Button>(R.id.logout_button)

        // 設置登出按鈕的點擊事件
        logoutButton.setOnClickListener {
            auth.signOut()  // 執行 Firebase 的登出操作
            // 登出後導航回登入頁面 (NotificationsFragment)
            findNavController().navigate(R.id.action_accountFragment_to_notificationsFragment)
        }

        // 設置 My Information 按鈕的點擊事件
        val myInfoButton = view.findViewById<Button>(R.id.my_info_button)
        myInfoButton.setOnClickListener {
            // 導航到 AccountInfoFragment
            findNavController().navigate(R.id.action_accountFragment_to_accountInfoFragment)
        }

        return view
    }
}
