import android.os.Bundle
import android.widget.EditText
import android.widget.RatingBar
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.R
import com.example.myapplication.toilethero.review.Review
import com.example.myapplication.toilethero.review.ReviewsAdapter
import com.example.myapplication.toilethero.toiletProfile.ToiletProfileFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import io.mockk.slot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue



@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [33],
    manifest = Config.NONE,
    packageName = "com.example.myapplication"
)
class ToiletProfileUnitTest {

    private val mockFirebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    private val mockDatabaseReference = mockk<DatabaseReference>(relaxed = true)
    private val mockFirebaseDatabase = mockk<FirebaseDatabase>(relaxed = true)

    private lateinit var fragment: ToiletProfileFragment
    private lateinit var mockReviewsAdapter: ReviewsAdapter

    @Before
    fun setup() {
        mockkStatic(FirebaseAuth::class)
        mockkStatic(FirebaseDatabase::class)

        every { FirebaseAuth.getInstance() } returns mockFirebaseAuth
        every { FirebaseDatabase.getInstance() } returns mockFirebaseDatabase
        every { mockFirebaseDatabase.getReference(any()) } returns mockDatabaseReference

        // 模擬 setValue 返回成功的 Task<Void>
        every { mockDatabaseReference.setValue(any()) } answers {
            Tasks.forResult(null)
        }

        // 使用 FragmentScenario 啟動 Fragment
        val scenario = launchFragmentInContainer<ToiletProfileFragment>(
            fragmentArgs = Bundle(),
            themeResId = R.style.Theme_MyApplication
        )
        scenario.onFragment {
            fragment = it
        }

        // 模擬 ReviewsAdapter，避免操作真正的 UI
        mockReviewsAdapter = mockk(relaxed = true)
        fragment.reviewsAdapter = mockReviewsAdapter

        // 初始化 reviewList
        fragment.reviewList = mutableListOf()

        // 初始化 EditText 和 RatingBar
        fragment.reviewTitle = EditText(fragment.requireContext())
        fragment.reviewBody = EditText(fragment.requireContext())
        fragment.toiletRating = RatingBar(fragment.requireContext())

        // 模擬輸入框初始值
        fragment.reviewTitle.setText("Test Title")
        fragment.reviewBody.setText("Test Body")
        fragment.toiletRating.rating = 4.5f

        // 初始化 Mock 的 Firebase Database Reference
        fragment.database_restrooms = mockDatabaseReference
        fragment.database_reviews = mockDatabaseReference
    }

    @Test
    fun testFetchToiletDetails() {
        val roomId = "298"

        // 模擬 Firebase 數據庫返回的數據
        val mockData = mapOf(
            "buildingName" to "Test Building",
            "roomNumber" to "101",
            "street" to "123 Test St",
            "averageOverallScore" to 4.5f,
            "reviewsCount" to 10L
        )

        every { mockDatabaseReference.child(roomId) } returns mockDatabaseReference
        every { mockDatabaseReference.setValue(mockData) } answers {
            Tasks.forResult(null) // 返回成功的 Task<Void>
        }

        // 啟動 Fragment
        val scenario = launchFragmentInContainer<ToiletProfileFragment>(
            fragmentArgs = Bundle().apply { putString("roomID", roomId) },
            themeResId = R.style.Theme_MyApplication
        )

        scenario.onFragment { fragment ->
            // 測試 FirebaseAuth 的調用
            verify { FirebaseAuth.getInstance() }

            // 測試 FirebaseDatabase 的調用
            verify { FirebaseDatabase.getInstance() }

            // 確認數據是否正確顯示
            fragment.fetchToiletDetails(roomId)
        }
    }

    @Test
    fun testLoadReviews_withOneReview_updatesReviewList() {
        val mockReview = Review(
            userID = "user1",
            reviewTitle = "Test Review",
            reviewBody = "This is a test review.",
            rating = 4.5f
        )
        fragment.reviewList.add(mockReview)

        // notify Adapter to update
        fragment.reviewsAdapter.notifyDataSetChanged()

        // check if reviewList is updated
        assertEquals(1, fragment.reviewList.size)
        val review = fragment.reviewList[0]
        assertEquals("Test Review", review.reviewTitle)
        assertEquals("This is a test review.", review.reviewBody)
        assertEquals(4.5f, review.rating)

        // check if Adapter is updated
        verify { fragment.reviewsAdapter.notifyDataSetChanged() }
    }
}