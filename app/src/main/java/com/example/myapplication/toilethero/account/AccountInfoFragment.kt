package com.example.myapplication.toilethero.account
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.toilethero.account.AccountRepository
import kotlinx.coroutines.launch


open class AccountInfoFragment(
    private val repository: AccountRepository = AccountRepository() // 默认使用真实的 Repository
) : Fragment() {

    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var dobEditText: EditText
    private lateinit var changeInfoButton: Button
    private lateinit var saveChangeButton: Button
    private lateinit var logoutButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account_info, container, false)

        firstNameEditText = view.findViewById(R.id.first_name_edit_text)
        lastNameEditText = view.findViewById(R.id.last_name_edit_text)
        emailEditText = view.findViewById(R.id.email_edit_text)
        phoneEditText = view.findViewById(R.id.phone_edit_text)
        dobEditText = view.findViewById(R.id.dob_edit_text)
        changeInfoButton = view.findViewById(R.id.change_info_button)
        saveChangeButton = view.findViewById(R.id.save_change_button)
        logoutButton = view.findViewById(R.id.logout_button)

        getUserInfo()

        changeInfoButton.setOnClickListener {
            enableEditing(true)
        }

        saveChangeButton.setOnClickListener {
            saveUserInfo()
        }

        logoutButton.setOnClickListener {
            repository.signOut()
            findNavController().navigate(R.id.action_accountFragment_to_notificationsFragment)
        }

        return view
    }

    fun getUserInfo() {
        lifecycleScope.launch {
            val userData = repository.getUserData()
            userData?.let {
                firstNameEditText.setText(it["firstName"])
                lastNameEditText.setText(it["lastName"])
                emailEditText.setText(it["email"])
                phoneEditText.setText(it["phone"])
                dobEditText.setText(it["dob"])
            }
        }
    }

    fun saveUserInfo() {
        val updatedData = mapOf(
            "firstName" to firstNameEditText.text.toString(),
            "lastName" to lastNameEditText.text.toString(),
            "email" to emailEditText.text.toString(),
            "phone" to phoneEditText.text.toString(),
            "dob" to dobEditText.text.toString()
        )

        lifecycleScope.launch {
            val isSuccess = repository.updateUserData(updatedData)
            if (isSuccess) {
                enableEditing(false)
            } else {
                // 处理更新失败
            }
        }
    }

    private fun enableEditing(enable: Boolean) {
        firstNameEditText.isEnabled = enable
        lastNameEditText.isEnabled = enable
        emailEditText.isEnabled = enable
        phoneEditText.isEnabled = enable
        dobEditText.isEnabled = enable
        changeInfoButton.visibility = if (enable) View.GONE else View.VISIBLE
        saveChangeButton.visibility = if (enable) View.VISIBLE else View.GONE
    }
}
