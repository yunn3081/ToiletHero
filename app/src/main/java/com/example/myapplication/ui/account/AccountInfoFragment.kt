package com.example.myapplication.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AccountInfoFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String

    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var dobEditText: EditText
    private lateinit var changeInfoButton: Button
    private lateinit var saveChangeButton: Button

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
        firstNameEditText = view.findViewById(R.id.first_name_edit_text)
        lastNameEditText = view.findViewById(R.id.last_name_edit_text)
        emailEditText = view.findViewById(R.id.email_edit_text)
        phoneEditText = view.findViewById(R.id.phone_edit_text)
        dobEditText = view.findViewById(R.id.dob_edit_text)
        changeInfoButton = view.findViewById(R.id.change_info_button)
        saveChangeButton = view.findViewById(R.id.save_change_button)

        // 从Firebase读取用户数据
        getUserInfo()

        // 设置"Change Information"按钮点击事件
        changeInfoButton.setOnClickListener {
            enableEditing(true)
        }

        // 设置"Save Change"按钮点击事件
        saveChangeButton.setOnClickListener {
            updateUserInfo()
            enableEditing(false) // 禁用编辑状态
        }

        return view
    }

    private fun getUserInfo() {
        database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    firstNameEditText.setText(snapshot.child("firstName").getValue(String::class.java))
                    lastNameEditText.setText(snapshot.child("lastName").getValue(String::class.java))
                    emailEditText.setText(snapshot.child("email").getValue(String::class.java))
                    phoneEditText.setText(snapshot.child("phone").getValue(String::class.java))
                    dobEditText.setText(snapshot.child("dob").getValue(String::class.java))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // 处理错误
            }
        })
    }

    private fun updateUserInfo() {
        // 更新用户信息到Firebase
        val updatedData = mapOf(
            "firstName" to firstNameEditText.text.toString(),
            "lastName" to lastNameEditText.text.toString(),
            "email" to emailEditText.text.toString(),
            "phone" to phoneEditText.text.toString(),
            "dob" to dobEditText.text.toString()
        )

        database.child(userId).updateChildren(updatedData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // 信息更新成功，可以在此添加一些提示或者动作
            } else {
                // 处理更新失败的情况
            }
        }
    }

    private fun enableEditing(enable: Boolean) {
        firstNameEditText.isEnabled = enable
        lastNameEditText.isEnabled = enable
        emailEditText.isEnabled = enable
        phoneEditText.isEnabled = enable
        dobEditText.isEnabled = enable
        saveChangeButton.visibility = if (enable) View.VISIBLE else View.GONE
    }
}
