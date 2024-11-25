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
        // inflate Fragment and load AccountInfoFragment
        val scenario = launchFragmentInContainer<AccountInfoFragment>(
            themeResId = R.style.Theme_MyApplication
        ) {
            AccountInfoFragment(FakeAccountRepository())
        }

        // check if user info are correct
        onView(withId(R.id.first_name_edit_text)).check(matches(withText("John")))
        onView(withId(R.id.last_name_edit_text)).check(matches(withText("Doe")))
        onView(withId(R.id.email_edit_text)).check(matches(withText("johndoe@example.com")))
        onView(withId(R.id.phone_edit_text)).check(matches(withText("1234567890")))
        onView(withId(R.id.dob_edit_text)).check(matches(withText("1990-01-01")))
    }

    @Test
    fun testEditUserInfo() {
        // inflate Fragment and load AccountInfoFragment
        val scenario = launchFragmentInContainer<AccountInfoFragment>(
            themeResId = R.style.Theme_MyApplication
        ) {
            AccountInfoFragment(FakeAccountRepository())
        }

        // click "Change Info"
        onView(withId(R.id.change_info_button)).perform(click())

        // change user info
        onView(withId(R.id.first_name_edit_text)).perform(clearText(), typeText("Jane"))
        onView(withId(R.id.last_name_edit_text)).perform(clearText(), typeText("Smith"))
        onView(withId(R.id.email_edit_text)).perform(clearText(), typeText("janesmith@example.com"))
        onView(withId(R.id.phone_edit_text)).perform(clearText(), typeText("0987654321"))
        onView(withId(R.id.dob_edit_text)).perform(clearText(), typeText("1992-02-02"))

        //  "Save Change" and save change
        onView(withId(R.id.save_change_button)).perform(click())

        onView(withId(R.id.change_info_button)).check(matches(isDisplayed()))
        onView(withId(R.id.save_change_button)).check(matches(not(isDisplayed())))
    }

    @Test
    fun testLogoutButton() {
        // init TestNavHostController
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        // init NavController
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            navController.setGraph(R.navigation.mobile_navigation)
            navController.setCurrentDestination(R.id.accountInfoFragment)
        }

        // init Fragment and bind NavController
        val scenario = launchFragmentInContainer<AccountInfoFragment>(
            themeResId = R.style.Theme_MyApplication
        ) {
            AccountInfoFragment(FakeAccountRepository())
        }

        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        // check if Logout is visiable
        onView(withId(R.id.logout_button)).check(matches(isDisplayed()))

        //  Logout button
        onView(withId(R.id.logout_button)).perform(click())

        // check if been directed NotificationsFragment
        assertEquals(R.id.notificationsFragment, navController.currentDestination?.id)
    }
}