package com.example.myapplication

import android.os.Looper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.myapplication.toilethero.account.AccountInfoFragment
import com.example.myapplication.toilethero.account.AccountRepository
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
@LooperMode(LooperMode.Mode.PAUSED)
class AccountInfoUnitTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: AccountRepository

    @Before
    fun setup() {
        println("🛠️ Setting up test environment...")

        // Mock AccountRepository
        repository = mockk(relaxed = true)
    }

    @Test
    fun `getUserInfo should populate UI with user data`() = runTest {
        println("🧪 Testing: getUserInfo populates UI with user data")

        // 模擬用戶數據
        val mockData = mapOf(
            "firstName" to "John",
            "lastName" to "Doe",
            "email" to "johndoe@example.com",
            "phone" to "1234567890",
            "dob" to "1990-01-01"
        )

        coEvery { repository.getUserData() } returns mockData

        val scenario = launchFragmentInContainer<AccountInfoFragment>(
            themeResId = R.style.Theme_MyApplication
        ) {
            AccountInfoFragment(repository)
        }

        scenario.onFragment { fragment ->
            fragment.getUserInfo()

            // 處理所有異步任務
            shadowOf(Looper.getMainLooper()).idle()

            // 驗證 UI 是否正確更新
            Assert.assertEquals("John", fragment.firstNameEditText.text.toString())
            Assert.assertEquals("Doe", fragment.lastNameEditText.text.toString())
            Assert.assertEquals("johndoe@example.com", fragment.emailEditText.text.toString())
            Assert.assertEquals("1234567890", fragment.phoneEditText.text.toString())
            Assert.assertEquals("1990-01-01", fragment.dobEditText.text.toString())
        }

        println("✅ Test passed: getUserInfo populated UI with correct data.")
        println("======================================================================================================")
    }

    @Test
    fun `saveUserInfo should call repository updateUserData with updated data`() = runBlocking {
        println("🧪 Testing: saveUserInfo calls repository updateUserData")

        coEvery { repository.updateUserData(any()) } returns true

        val scenario = launchFragmentInContainer<AccountInfoFragment>(
            themeResId = R.style.Theme_MyApplication
        ) {
            AccountInfoFragment(repository)
        }

        scenario.onFragment { fragment ->
            // 模擬用戶在 UI 中更新數據
            fragment.firstNameEditText.setText("Jane")
            fragment.lastNameEditText.setText("Doe")
            fragment.emailEditText.setText("janedoe@example.com")
            fragment.phoneEditText.setText("0987654321")
            fragment.dobEditText.setText("1992-02-02")

            fragment.saveUserInfo()

            // 驗證 Repository 是否被正確調用
            coVerify {
                repository.updateUserData(
                    mapOf(
                        "firstName" to "Jane",
                        "lastName" to "Doe",
                        "email" to "janedoe@example.com",
                        "phone" to "0987654321",
                        "dob" to "1992-02-02"
                    )
                )
            }
        }

        println("✅ Test passed: saveUserInfo called repository updateUserData with correct data.")
        println("======================================================================================================")

    }

    @Test
    fun `logoutButton should call signOut and navigate`() = runBlocking {
        println("🧪 Testing: logoutButton calls signOut and navigates")

        // 模擬 NavController
        val mockNavController = mockk<NavController>(relaxed = true)

        val scenario = launchFragmentInContainer<AccountInfoFragment>(
            themeResId = R.style.Theme_MyApplication
        ) {
            AccountInfoFragment(repository)
        }

        scenario.onFragment { fragment ->
            // 將 NavController 綁定到 Fragment 的視圖
            Navigation.setViewNavController(fragment.requireView(), mockNavController)

            // 模擬按下登出按鈕
            fragment.logoutButton.performClick()

            // 驗證 NavController 是否正確導航
            verify {
                mockNavController.navigate(R.id.action_accountFragment_to_notificationsFragment)
            }

            // 驗證 signOut 方法是否被調用
            coVerify { repository.signOut() }
        }

        println("✅ Test passed: logoutButton triggered signOut and navigation.")
        println("======================================================================================================")

    }

}
