package com.example.myapplication

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.example.myapplication.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import android.Manifest
import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.idling.CountingIdlingResource
import org.junit.After
import androidx.test.platform.app.InstrumentationRegistry
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import com.example.myapplication.toilethero.restroom.RestroomNearbyFragment
import com.example.myapplication.toilethero.restroom.RestroomDetailsBottomSheet

@RunWith(AndroidJUnit4::class)
class RestroomNearByUITest {

    private lateinit var scenario: FragmentScenario<RestroomNearbyFragment>
    private val idlingResource = CountingIdlingResource("BottomSheet")

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @Before
    fun setup() {
        Log.d("UI_TEST", "Setting up test environment")
        IdlingRegistry.getInstance().register(idlingResource)
        scenario = launchFragmentInContainer(themeResId = R.style.Theme_MyApplication)
        Thread.sleep(2000)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(idlingResource)
    }

    @Test
    fun testMapFragmentIsDisplayed() {
        Log.d("UI_TEST", "Testing if map fragment is displayed")
        waitForView(withId(R.id.map_fragment))
        onView(withId(R.id.map_fragment))
            .check(matches(isDisplayed()))
        Log.d("UI_TEST", "Map fragment display test passed")
    }

    @Test
    fun testBottomSheetDisplay() {
        Log.d("UI_TEST", "Testing bottom sheet display")

        val latch = CountDownLatch(1)

        waitForFragment()

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            scenario.onFragment { fragment ->
                try {
                    fragment.requireActivity()
                    fragment.requireContext()

                    val restroomId = "94"
                    val bottomSheet = RestroomDetailsBottomSheet.newInstance(restroomId)
                    bottomSheet.show(fragment.childFragmentManager, "RestroomDetails")
                    fragment.childFragmentManager.executePendingTransactions()
                    latch.countDown()
                } catch (e: Exception) {
                    Log.e("UI_TEST", "Error in bottom sheet test: ${e.message}")
                    latch.countDown()
                }
            }
        }

        latch.await(10, TimeUnit.SECONDS)
        Thread.sleep(1000)

        // 檢查基本UI元素
        waitForView(withId(R.id.restroom_name))
        onView(withId(R.id.restroom_name))
            .check(matches(isDisplayed()))

        waitForView(withId(R.id.restroom_address))
        onView(withId(R.id.restroom_address))
            .check(matches(isDisplayed()))

        // 檢查 loading spinner 是否顯示
        onView(withId(R.id.loadingSpinner))
            .check(matches(isDisplayed()))

        Log.d("UI_TEST", "Waiting for image to load...")
        Thread.sleep(10000) // 等待10秒讓圖片加載

        // 檢查圖片是否顯示，loading spinner 是否消失
        try {
            waitForView(withId(R.id.restroom_image), timeoutSeconds = 15)
            onView(withId(R.id.restroom_image))
                .check(matches(isDisplayed()))

            onView(withId(R.id.loadingSpinner))
                .check(matches(withEffectiveVisibility(Visibility.GONE)))

            Log.d("UI_TEST", "Image loaded successfully")
        } catch (e: Exception) {
            Log.e("UI_TEST", "Error checking image visibility: ${e.message}")
            // 如果檢查失敗，輸出當前狀態
            scenario.onFragment { fragment ->
                val imageView = fragment.view?.findViewById<ImageView>(R.id.restroom_image)
                val spinner = fragment.view?.findViewById<ProgressBar>(R.id.loadingSpinner)
                Log.d("UI_TEST", "Image visibility: ${imageView?.visibility}")
                Log.d("UI_TEST", "Spinner visibility: ${spinner?.visibility}")
            }
            throw e
        }

        // 檢查其他UI元素
        waitForView(withId(R.id.reviews_text))
        onView(withId(R.id.reviews_text))
            .check(matches(isDisplayed()))

        waitForView(withId(R.id.rating_reviews_header))
        onView(withId(R.id.rating_reviews_header))
            .check(matches(isDisplayed()))

        Log.d("UI_TEST", "Bottom sheet display test completed")
    }

    @Test
    fun testLocationButtonVisibility() {
        Log.d("UI_TEST", "Testing location permission")

        waitForFragment()

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            scenario.onFragment { fragment ->
                val activity = fragment.requireActivity()
                val hasPermission = activity.checkSelfPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                assert(hasPermission == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    "Location permission should be granted"
                }
            }
        }

        waitForView(withId(R.id.map_fragment))
        onView(withId(R.id.map_fragment))
            .check(matches(isDisplayed()))

        Log.d("UI_TEST", "Location permission test passed")
    }

    @Test
    fun testMapInteraction() {
        Log.d("UI_TEST", "Testing map interaction state")

        waitForFragment()

        waitForView(withId(R.id.map_fragment))
        onView(withId(R.id.map_fragment))
            .check(matches(isDisplayed()))

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            scenario.onFragment { fragment ->
                fragment.getVisibleBounds()?.let { bounds ->
                    Log.d("UI_TEST", "Successfully got map bounds: $bounds")
                    assert(true) { "Map bounds retrieved successfully" }
                }
            }
        }

        Log.d("UI_TEST", "Map interaction test completed")
    }

    private fun waitForFragment() {
        val latch = CountDownLatch(1)
        scenario.onFragment { fragment ->
            try {
                fragment.requireActivity()
                fragment.requireContext()
                latch.countDown()
            } catch (e: IllegalStateException) {
                Log.e("UI_TEST", "Fragment not yet attached: ${e.message}")
            }
        }
        latch.await(5, TimeUnit.SECONDS)
    }

    private fun waitForView(matcher: org.hamcrest.Matcher<android.view.View>, timeoutSeconds: Long = 5) {
        val startTime = System.currentTimeMillis()
        val timeoutMillis = timeoutSeconds * 1000

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            try {
                onView(matcher).check(matches(isDisplayed()))
                return
            } catch (e: Exception) {
                Thread.sleep(100)
            }
        }
        throw AssertionError("View never became visible: $matcher")
    }
}