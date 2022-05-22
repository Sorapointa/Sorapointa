package org.sorapointa.dispatch

import io.ktor.util.*
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.sorapointa.data.provider.DatabaseManager
import org.sorapointa.data.provider.initTestDataProvider
import org.sorapointa.dispatch.data.Account
import org.sorapointa.dispatch.data.AccountTable
import org.sorapointa.utils.TestOption
import org.sorapointa.utils.crypto.randomByteArray
import org.sorapointa.utils.encoding.hex
import org.sorapointa.utils.runTest
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountTest {

    @BeforeAll
    fun init() = runTest(TestOption.SKIP_CI) {
        initTestDataProvider()
        DispatchConfig.init()
        DatabaseManager.loadTables(AccountTable)
    }

    @Test
    fun `create account test`() = runTest(TestOption.SKIP_CI) {

        newSuspendedTransaction {
            AccountTable.deleteWhere { AccountTable.userName eq "foobar" }
            Account.create("foobar", "password")
            assertEquals(1, Account.findByName("foobar").count())
        }
    }

    @Test
    fun `password test`() = runTest(TestOption.SKIP_CI) {

        newSuspendedTransaction {
            AccountTable.deleteWhere { AccountTable.userName eq "foobar" }

            Account.create("foobar", "password123")

            val account = Account.findByName("foobar").first()

            assertEquals(true, account.checkPassword("password123"))
            assertEquals(false, account.checkPassword("nmsl"))

            account.updatePassword("123password")

            assertEquals(false, account.checkPassword("password123"))
            assertEquals(true, account.checkPassword("123password"))
        }
    }

    @Test
    fun `high volume test`() = runTest(TestOption.SKIP_CI) {

        val totalTestCount = 1000

        data class TestAccountInfo(
            val name: String,
            val pwd: String
        )

        val testAccountMap = (1..totalTestCount).map {
            TestAccountInfo(randomByteArray(16).hex, randomByteArray(256).encodeBase64())
        }

        testAccountMap.map { testInfo ->
            launch {
                newSuspendedTransaction {
                    val account = Account.findOrCreate(testInfo.name, testInfo.pwd)

                    val newPwd = randomByteArray(256).encodeBase64()

                    assertEquals(true, account.checkPassword(testInfo.pwd))
                    assertEquals(false, account.checkPassword(newPwd))

                    account.updatePassword(newPwd)

                    assertEquals(false, account.checkPassword(testInfo.pwd))
                    assertEquals(true, account.checkPassword(newPwd))

                    account.updatePassword(testInfo.pwd)
                }
            }
        }.joinAll()

        transaction {
            AccountTable.deleteAll()
        }
    }
}
