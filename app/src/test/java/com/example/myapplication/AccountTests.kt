import com.example.myapplication.UserInfo
import com.example.myapplication.toilethero.account.AccountRepository
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class AccountTests {

    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockDatabase: DatabaseReference
    private lateinit var repository: AccountRepository
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val mockFirebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    private val mockDatabaseReference = mockk<DatabaseReference>(relaxed = true)
    private val mockFirebaseDatabase = mockk<FirebaseDatabase>(relaxed = true)

    @Before
    fun setup() {
        println("🚀 Setting up mocks and initializing repository")
        mockkStatic(FirebaseAuth::class)
        mockkStatic(FirebaseDatabase::class)

        every { FirebaseAuth.getInstance() } returns mockFirebaseAuth
        every { FirebaseDatabase.getInstance() } returns mockFirebaseDatabase
        every { mockFirebaseDatabase.getReference(any()) } returns mockDatabaseReference

        // 模拟 setValue 成功
        every { mockDatabaseReference.setValue(any()) } answers {
            Tasks.forResult(null)
        }

        // 初始化 repository
        repository = AccountRepository(auth = mockFirebaseAuth, database = mockDatabaseReference)
        println("✅ Setup complete")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        println("🧹 Test environment cleaned up")
    }

    @Test
    fun `test user info formatting`() {
        println("🧪 Testing user info formatting")
        val userInfo = UserInfo("John", "Doe", "john.doe@example.com")
        val formattedInfo = formatUserInfo(userInfo)
        println("🔍 Input UserInfo: $userInfo")
        println("🔍 Formatted UserInfo: $formattedInfo")
        assertEquals("John Doe (john.doe@example.com)", formattedInfo)
        println("✅ Test passed: User info formatted correctly")
        println("======================================================================")
    }

    @Test
    fun `test user email validation`() {
        println("🧪 Testing email validation")
        val emails = listOf(
            "john.doe@example.com" to true,
            "invalid-email" to false,
            "test@domain" to false,
            "user.name+tag+sorting@example.com" to true
        )
        println("🔍 Testing emails: $emails")
        emails.forEach { (email, expected) ->
            val isValid = validateEmail(email)
            println("🔍 Email: $email, Is valid: $isValid")
            assertEquals(expected, isValid)
            println("✅ Test passed for email: $email")
        }
        println("======================================================================")
    }

    @Test
    fun `test repository getUserData returns correct data`() = testScope.runTest {
        println("🧪 Testing repository getUserData")
        // Mock DataSnapshot
        val mockSnapshot = mockk<DataSnapshot>(relaxed = true)

        every { mockSnapshot.child("firstName").getValue(String::class.java) } returns "John"
        every { mockSnapshot.child("lastName").getValue(String::class.java) } returns "Doe"
        every { mockSnapshot.child("email").getValue(String::class.java) } returns "john.doe@example.com"
        every { mockSnapshot.child("phone").getValue(String::class.java) } returns "1234567890"
        every { mockSnapshot.child("dob").getValue(String::class.java) } returns "01-01-1990"
        every { mockSnapshot.exists() } returns true

        // 模拟 DatabaseReference.get() 的行为，返回成功的 Task<DataSnapshot>
        val mockTask = Tasks.forResult(mockSnapshot)
        every { mockDatabaseReference.child("mockUserId").get() } returns mockTask

        // 模拟 FirebaseAuth.currentUser
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        every { mockFirebaseAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "mockUserId"

        println("🔍 Mocking complete, calling getUserData()")
        val result = repository.getUserData()

        // 验证结果
        assertNotNull(result)
        println("🔍 Result: $result")
        assertEquals("John", result?.get("firstName"))
        assertEquals("Doe", result?.get("lastName"))
        assertEquals("john.doe@example.com", result?.get("email"))
        assertEquals("1234567890", result?.get("phone"))
        assertEquals("01-01-1990", result?.get("dob"))
        println("✅ Test passed: getUserData returned correct data")
        println("======================================================================")
    }

    private fun formatUserInfo(userInfo: UserInfo): String {
        return "${userInfo.firstName} ${userInfo.lastName} (${userInfo.email})"
    }

    private fun validateEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }
}