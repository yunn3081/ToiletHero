package com.example.myapplication

import com.example.myapplication.toilethero.restroom.RestroomDetailsBottomSheet
import io.mockk.*
import kotlinx.coroutines.test.runTest
import okhttp3.*
import okhttp3.Call
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class RestroomDetailsBottomSheetUnitTest {

    private lateinit var restroomDetailsBottomSheet: RestroomDetailsBottomSheet
    private val mockClient: OkHttpClient = mockk(relaxed = true)

    @Before
    fun setup() {
        println("🛠️ Setting up test environment...")
        // 初始化 RestroomDetailsBottomSheet
        restroomDetailsBottomSheet = spyk(RestroomDetailsBottomSheet())

        // 使用反射設置 private client
        val clientField = restroomDetailsBottomSheet.javaClass.getDeclaredField("client")
        clientField.isAccessible = true
        clientField.set(restroomDetailsBottomSheet, mockClient)
    }

    @Test
    fun `fetchPlaceId should return place IDs when response is successful`() {
        println("🧪 Testing: fetchPlaceId should return place IDs when response is successful")
        val placeIds = mutableListOf<String>()
        every { restroomDetailsBottomSheet.fetchPlaceId(any(), any(), captureLambda()) } answers {
            lambda<(List<String>?) -> Unit>().invoke(listOf("place1", "place2"))
        }

        restroomDetailsBottomSheet.fetchPlaceId(40.748817, -73.985428) {
            placeIds.addAll(it ?: emptyList())
        }

        // 驗證返回的數據
        assertEquals(2, placeIds.size)
        assertEquals("place1", placeIds[0])
        assertEquals("place2", placeIds[1])
        println("✅ Test passed: fetchPlaceId returned correct place IDs.")
        println("======================================================================")

    }

    @Test
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun `fetchPhotoReference should return photo reference when response is successful`() = runTest {
        println("🧪 Testing: fetchPhotoReference should return photo reference when response is successful")
        // 模擬 API 返回的 JSON 數據
        val mockResponseBody = """
        {
            "result": {
                "photos": [
                    {"photo_reference": "photo123"}
                ]
            }
        }
        """.trimIndent()

        val mockResponseBodyObj = mockk<ResponseBody> {
            every { string() } returns mockResponseBody
        }

        val mockResponse = mockk<Response> {
            every { isSuccessful } returns true
            every { body } returns mockResponseBodyObj
        }

        val mockCall = mockk<Call>()
        every { mockCall.enqueue(any()) } answers {
            val callback = firstArg<okhttp3.Callback>()
            callback.onResponse(mockCall, mockResponse)
        }

        every { mockClient.newCall(any()) } returns mockCall

        var photoReference: String? = null
        restroomDetailsBottomSheet.fetchPhotoReference("place1") {
            photoReference = it
            println("📷 Photo reference received: $photoReference")
        }

        // 驗證返回的數據
        assertEquals("photo123", photoReference)
        println("✅ Test passed: fetchPhotoReference returned correct photo reference.")
        println("======================================================================")

    }

    @Test
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun `fetchPhotoReference should return null when response is unsuccessful`() = runTest {
        println("🧪 Testing: fetchPhotoReference should return null when response is unsuccessful")
        val mockResponse = mockk<Response> {
            every { isSuccessful } returns false
        }

        val mockCall = mockk<Call>(relaxed = true)
        every { mockCall.enqueue(any()) } answers {
            val callback = firstArg<okhttp3.Callback>()
            callback.onFailure(mockCall, IOException("Test error"))
        }

        every { mockClient.newCall(any()) } returns mockCall

        var photoReference: String? = null
        restroomDetailsBottomSheet.fetchPhotoReference("place1") {
            photoReference = it
            println("📷 Photo reference received: $photoReference")
        }

        // 驗證回傳結果為 null
        assertEquals(null, photoReference)
        println("✅ Test passed: fetchPhotoReference returned null for unsuccessful response.")
        println("======================================================================")
    }
}
