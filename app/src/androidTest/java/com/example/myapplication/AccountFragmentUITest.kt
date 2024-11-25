package com.example.myapplication.toilethero.account

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountFragmentUITest {

    private lateinit var scenario: FragmentScenario<AccountFragment>

    @Before
    fun setup() {
        // use AppCompatActivity instead of EmptyFragmentActivity
        scenario = launchFragmentInContainer<AccountFragment>(
            themeResId = R.style.Theme_MyApplication
        ) {
            AccountFragment()
        }
    }

    @Test
    fun testGreetingTextIsDisplayed() {
        // check if greetingText contains "Hi,"
        onView(withId(R.id.greetingText))
            .check(matches(isDisplayed()))
            .check(matches(withSubstring("Hi,")))
    }

    @Test
    fun testSettingsButtonIsDisplayed() {
        // check if settings_button shows up
        onView(withId(R.id.settings_button))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testReviewFragmentIsLoaded() {
        // check if review_fragment_container shows up
        onView(withId(R.id.review_fragment_container))
            .check(matches(isDisplayed()))
    }
}
