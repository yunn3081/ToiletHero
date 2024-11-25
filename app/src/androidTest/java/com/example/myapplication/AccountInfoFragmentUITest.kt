package com.example.myapplication

import FakeAccountRepository
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.toilethero.account.AccountInfoFragment
import junit.framework.TestCase.assertEquals
import org.hamcrest.CoreMatchers.not
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.platform.app.InstrumentationRegistry

@RunWith(AndroidJUnit4::class)
class AccountInfoFragmentUITest {

    @Test
    fun testUserInfoDisplayedCorrectly() {
        // 启动测试 Fragment 并加载 AccountInfoFragment
        val scenario = launchFragmentInContainer<AccountInfoFragment>(
            themeResId = R.style.Theme_MyApplication
        ) {
            AccountInfoFragment(FakeAccountRepository())
        }

        // 验证用户信息是否正确显示
        onView(withId(R.id.first_name_edit_text)).check(matches(withText("John")))
        onView(withId(R.id.last_name_edit_text)).check(matches(withText("Doe")))
        onView(withId(R.id.email_edit_text)).check(matches(withText("johndoe@example.com")))
        onView(withId(R.id.phone_edit_text)).check(matches(withText("1234567890")))
        onView(withId(R.id.dob_edit_text)).check(matches(withText("1990-01-01")))
    }

    @Test
    fun testEditUserInfo() {
        // 启动测试 Fragment 并加载 AccountInfoFragment
        val scenario = launchFragmentInContainer<AccountInfoFragment>(
            themeResId = R.style.Theme_MyApplication
        ) {
            AccountInfoFragment(FakeAccountRepository())
        }

        // 点击 "Change Info" 按钮启用编辑模式
        onView(withId(R.id.change_info_button)).perform(click())

        // 修改用户信息
        onView(withId(R.id.first_name_edit_text)).perform(clearText(), typeText("Jane"))
        onView(withId(R.id.last_name_edit_text)).perform(clearText(), typeText("Smith"))
        onView(withId(R.id.email_edit_text)).perform(clearText(), typeText("janesmith@example.com"))
        onView(withId(R.id.phone_edit_text)).perform(clearText(), typeText("0987654321"))
        onView(withId(R.id.dob_edit_text)).perform(clearText(), typeText("1992-02-02"))

        // 点击 "Save Change" 按钮保存更改
        onView(withId(R.id.save_change_button)).perform(click())

        // 验证按钮状态是否切换回非编辑模式
        onView(withId(R.id.change_info_button)).check(matches(isDisplayed()))
        onView(withId(R.id.save_change_button)).check(matches(not(isDisplayed())))
    }

    @Test
    fun testLogoutButton() {
        // 初始化 TestNavHostController
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        // 在主线程中初始化 NavController
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            navController.setGraph(R.navigation.mobile_navigation)
            navController.setCurrentDestination(R.id.accountInfoFragment)
        }

        // 启动 Fragment 并绑定 NavController
        val scenario = launchFragmentInContainer<AccountInfoFragment>(
            themeResId = R.style.Theme_MyApplication
        ) {
            AccountInfoFragment(FakeAccountRepository())
        }

        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        // 验证 Logout 按钮可见
        onView(withId(R.id.logout_button)).check(matches(isDisplayed()))

        // 点击 Logout 按钮
        onView(withId(R.id.logout_button)).perform(click())

        // 验证是否导航到了 NotificationsFragment
        assertEquals(R.id.notificationsFragment, navController.currentDestination?.id)
    }
}