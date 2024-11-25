import com.example.myapplication.toilethero.account.AccountRepository

class FakeAccountRepository : AccountRepository() {

    private val mockData = mutableMapOf(
        "firstName" to "John",
        "lastName" to "Doe",
        "email" to "johndoe@example.com",
        "phone" to "1234567890",
        "dob" to "1990-01-01"
    )

    override suspend fun getUserData(): Map<String, String>? {
        return mockData
    }

    override suspend fun updateUserData(data: Map<String, String>): Boolean {
        mockData.putAll(data)
        return true
    }

    override fun signOut() {
        // 模拟登出逻辑
    }
}