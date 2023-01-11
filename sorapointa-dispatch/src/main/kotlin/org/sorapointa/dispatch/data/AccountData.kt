package org.sorapointa.dispatch.data

import com.password4j.Argon2Function
import com.password4j.Password
import io.ktor.util.*
import kotlinx.datetime.Instant
import mu.KotlinLogging
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.sorapointa.dispatch.DispatchConfig
import org.sorapointa.dispatch.events.CreateAccountEvent
import org.sorapointa.event.broadcast
import org.sorapointa.utils.SorapointaInternal
import org.sorapointa.utils.encoding.hex
import org.sorapointa.utils.now
import org.sorapointa.utils.randomByteArray
import org.sorapointa.utils.randomUInt

private val logger = KotlinLogging.logger {}

@SorapointaInternal
object AccountTable : IdTable<Int>("account_table") {
    override val id: Column<EntityID<Int>> = integer("user_id").autoIncrement().entityId()
    val userName: Column<String> = varchar("user_name", 60).uniqueIndex()
    val password: Column<String> = varchar("password", 255)
    val email: Column<String?> = varchar("email", 255).uniqueIndex().nullable()
    val comboToken: Column<String?> = varchar("combo_token", 40).nullable()
    val comboTokenGenerationTime: Column<Instant?> = timestamp("combo_token_generation_time").nullable()
    val comboId: Column<Int?> = integer("combo_id").nullable()
    val dispatchToken: Column<String?> = varchar("dispatch_token", 60).nullable()
    val dispatchTokenGenerationTime: Column<Instant?> = timestamp("dispatch_token_generation_time").nullable()
    val permissionLevel: Column<Int> = integer("permission_level").default(0)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

@SorapointaInternal
val argon2Function: Argon2Function by lazy {
    val setting = DispatchConfig.data.accountSetting.password
    Argon2Function.getInstance(
        setting.memory,
        setting.iterations,
        setting.parallelism,
        setting.byteLength,
        setting.argon2Type,
        setting.argon2Version,
    )
}

@Suppress("MemberVisibilityCanBePrivate", "unused")
class Account(id: EntityID<Int>) : Entity<Int>(id) {

    companion object : EntityClass<Int, Account>(AccountTable) {

        private val usePepper = DispatchConfig.data.accountSetting.password.usePepper
        private val pepper = DispatchConfig.data.accountSetting.password.hashPepper

        /**
         * Find a user account by email
         */
        fun findByEmail(email: String) =
            find { AccountTable.email eq email }

        /**
         * Find a user account by username
         */
        fun findByName(name: String) =
            find { AccountTable.userName eq name }

        /**
         * Find a user account by username or create one
         *
         * @param name username
         * @param inputPassword plain password without hash
         * @return [Account]
         */
        suspend fun findOrCreate(name: String, inputPassword: String): Account =
            findByName(name).firstOrNull() ?: run { findById(create(name, inputPassword).value)!! }

        /**
         * Create an account to database
         *
         * @param name username
         * @param inputPassword plain password without hash
         * @return user id in databse
         */
        suspend fun create(name: String, inputPassword: String): EntityID<Int> {
            logger.info { "Creating user account of $name" }
            CreateAccountEvent(name).broadcast()
            val hashSalt = generateSalt()
            val pwd = hashPassword(inputPassword, hashSalt)
            return AccountTable.insertAndGetId {
                it[userName] = name
                it[password] = pwd
            }
        }

        private fun hashPassword(inputPassword: String, salt: String): String =
            Password.hash(inputPassword)
                .addSalt(salt)
                .apply {
                    if (usePepper) {
                        this.addPepper(pepper)
                    }
                }
                .with(argon2Function).result

        private fun generateSalt(): String =
            randomByteArray(DispatchConfig.data.accountSetting.password.saltByteLength).encodeBase64()
    }

    var userName by AccountTable.userName
    private var password by AccountTable.password
    var email by AccountTable.email
    private var comboToken by AccountTable.comboToken
    private var comboTokenGenerationTime by AccountTable.comboTokenGenerationTime
    private var comboId by AccountTable.comboId
    private var dispatchToken by AccountTable.dispatchToken
    private var dispatchTokenGenerationTime by AccountTable.dispatchTokenGenerationTime
    var permissionLevel by AccountTable.permissionLevel

    internal fun checkPassword(inputPassword: String): Boolean =
        Password.check(inputPassword, password)
            .apply {
                if (usePepper) {
                    this.addPepper(pepper)
                }
            }.with(argon2Function)

    @SorapointaInternal
    fun updatePassword(inputPassword: String) {
        password = hashPassword(inputPassword, generateSalt())
    }

    internal fun generateDispatchToken(): String {
        val token = randomByteArray(32).encodeBase64()
        dispatchToken = token
        dispatchTokenGenerationTime = now()
        return token
    }

    internal fun generateComboToken(): String {
        val token = randomByteArray(20).hex
        comboToken = token
        comboTokenGenerationTime = now()
        return token
    }

    // Permanent link to one user account
    internal fun getComboIdOrGenerate(): Int =
        comboId ?: randomUInt().toInt().also {
            comboId = it
        }

    @SorapointaInternal
    fun getComboTokenWithCheck(): String? {
        if (checkComboTokenExpire()) return null
        return comboToken
    }

    internal fun getDispatchToken(): String? {
        if (checkDispatchTokenExpire()) return null
        return dispatchToken
    }

    private fun checkComboTokenExpire(): Boolean =
        comboTokenGenerationTime?.let {
            now() - it > DispatchConfig.data.accountSetting.comboTokenExpiredTime
        } ?: true

    private fun checkDispatchTokenExpire(): Boolean =
        dispatchTokenGenerationTime?.let {
            now() - it > DispatchConfig.data.accountSetting.dispatchTokenExpiredTime
        } ?: true
}
