package com.example.myapplication.toilethero.toiletProfile

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import android.os.Bundle
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.CountingIdlingResource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.hamcrest.Matchers.not

@RunWith(AndroidJUnit4::class)
class ToiletProfileUITest {
    private lateinit var scenario: FragmentScenario<ToiletProfileFragment>
    private val testRoomId = "298"
    private val auth = FirebaseAuth.getInstance()

    @Before
    fun setup() {
        // 使用匿名登入
        anonymousLogin()

        // 傳遞測試用的房間 ID
        val bundle = Bundle().apply {
            putString("roomID", testRoomId)
        }

        // 啟動 Fragment 並傳遞參數
        scenario = launchFragmentInContainer(fragmentArgs = bundle, themeResId = R.style.Theme_MyApplication)
    }

    // 使用匿名登入的函數
    private fun anonymousLogin() {
        val latch = CountDownLatch(1)

        // 匿名登入
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    println("Anonymous login successful: ${auth.currentUser?.uid}")
                } else {
                    println("Anonymous login failed: ${task.exception?.message}")
                }
                latch.countDown()
            }

        // 等待登入完成
        latch.await(5, TimeUnit.SECONDS)
    }

    @Test
    fun testInitialUIElementsVisibility() {
        // 檢查基本 UI 元件是否顯示
        onView(withId(R.id.toilet_name)).check(matches(isDisplayed()))
        onView(withId(R.id.toilet_roomNumber)).check(matches(isDisplayed()))
        onView(withId(R.id.toilet_address)).check(matches(isDisplayed()))
        onView(withId(R.id.direction_icon)).check(matches(isDisplayed()))
        onView(withId(R.id.rating_reviews_title)).check(matches(isDisplayed()))
    }

    @Test
    fun testAuthenticationUIStates() {
        // 登入狀態檢查
        if (auth.currentUser != null) {
            onView(withId(R.id.submit_review_button)).check(matches(isDisplayed()))
            onView(withId(R.id.review_title)).check(matches(isDisplayed()))
            onView(withId(R.id.review_body)).check(matches(isDisplayed()))
            onView(withId(R.id.login_button)).check(matches(not(isDisplayed())))
        } else {
            onView(withId(R.id.submit_review_button)).check(matches(not(isDisplayed())))
            onView(withId(R.id.review_title)).check(matches(not(isDisplayed())))
            onView(withId(R.id.review_body)).check(matches(not(isDisplayed())))
            onView(withId(R.id.login_button)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun testReviewSubmission() {

        if (auth.currentUser != null) {
            val testTitle = "Test Review Title"
            val testBody = "This is a test review body"

            onView(withId(R.id.review_title)).perform(typeText(testTitle), closeSoftKeyboard())
            onView(withId(R.id.review_body)).perform(typeText(testBody), closeSoftKeyboard())
            onView(withId(R.id.toilet_rating)).perform(click())


            onView(withId(R.id.submit_review_button)).perform(click())
            Thread.sleep(2000)


            // 確認評論已提交並清空輸入框
            onView(withId(R.id.review_title)).check(matches(withText("")))
            onView(withId(R.id.review_body)).check(matches(withText("")))
        }

    }

    @Test
    fun testReviewsListDisplay() {
        // 測試評論列表是否正確顯示
        val latch = CountDownLatch(1)

        scenario.onFragment { fragment ->
            fragment.loadReviews(testRoomId)
            latch.countDown()
        }

        latch.await(5, TimeUnit.SECONDS)

        // 檢查評論列表或 "No reviews" 訊息是否顯示
        try {
            onView(withId(R.id.reviews_recycler_view)).check(matches(isDisplayed()))
        } catch (e: Exception) {
            onView(withId(R.id.no_reviews_text_view)).check(matches(isDisplayed()))
        }
    }
}
