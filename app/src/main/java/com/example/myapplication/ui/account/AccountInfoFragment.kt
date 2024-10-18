package com.example.myapplication.ui.account

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AccountInfoFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String

    private lateinit var firstNameTextView: TextView
    private lateinit var lastNameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var dobTextView: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_account_info, container, false)

        // 初始化 FirebaseAuth 和 Firebase Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")

        // 获取当前用户的ID
        userId = auth.currentUser?.uid ?: ""

        // 连接 UI 元素
        firstNameTextView = view.findViewById(R.id.first_name_text_view)
        lastNameTextView = view.findViewById(R.id.last_name_text_view)
        emailTextView = view.findViewById(R.id.email_text_view)
        phoneTextView = view.findViewById(R.id.phone_text_view)
        dobTextView = view.findViewById(R.id.dob_text_view)

        // 从Firebase读取用户数据
        getUserInfo()

        return view
    }

    private fun getUserInfo() {
        database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val firstName = snapshot.child("firstName").getValue(String::class.java)
                    val lastName = snapshot.child("lastName").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)
                    val phone = snapshot.child("phone").getValue(String::class.java)
                    val dob = snapshot.child("dob").getValue(String::class.java)

                    // 将数据设置到TextViews中
                    firstNameTextView.text = firstName
                    lastNameTextView.text = lastName
                    emailTextView.text = email
                    phoneTextView.text = phone
                    dobTextView.text = dob
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // 处理错误
            }
        })
    }
}
