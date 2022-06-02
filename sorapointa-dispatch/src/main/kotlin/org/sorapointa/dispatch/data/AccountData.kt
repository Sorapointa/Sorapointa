@file:OptIn(SorapointaInternal::class)

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
import org.sorapointa.utils.randomByteArray
import org.sorapointa.utils.encoding.hex
import org.sorapointa.utils.now
import org.sorapointa.utils.randomUInt

private val logger = KotlinLogging.logger {}

@SorapointaInternal object AccountTable : IdTable<UInt>("account_table") {
    override val id: Column<EntityID<UInt>> = uinteger("user_id").autoIncrement().entityId()
    val userName: Column<String> = varchar("user_name", 60).uniqueIndex()
    val password: Column<String> = varchar("password", 255)
    val email: Column<String?> = varchar("email", 255).uniqueIndex().nullable()
    val comboToken: Column<String?> = varchar("combo_token", 40).nullable()
    val comboTokenGenerationTime: Column<Instant?> = timestamp("combo_token_generation_time").nullable()
    val comboId: Column<UInt?> = uinteger("combo_id").nullable()
    val dispatchToken: Column<String?> = varchar("dispatch_token", 32).nullable()
    val dispatchTokenGenerationTime: Column<Instant?> = timestamp("dispatch_token_generation_time").nullable()
    val permissionLevel: Column<UShort> = ushort("permission_level").default(0u)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

@Suppress("RedundantSuspendModifier", "MemberVisibilityCanBePrivate", "unused")
class Account(id: EntityID<UInt>) : Entity<UInt>(id) {

    companion object : EntityClass<UInt, Account>(AccountTable) {

        private val usePepper = DispatchConfig.data.accountSetting.password.usePepper
        private val pepper = DispatchConfig.data.accountSetting.password.hashPepper

        private val argon2: Argon2Function by lazy {
            val setting = DispatchConfig.data.accountSetting.password
            Argon2Function.getInstance(
                setting.memory,
                setting.iterations,
                setting.parallelism,
                setting.byteLength,
                setting.argon2Type,
                setting.argon2Version
            )
        }

        /**
         * Find a user account by email
         */
        suspend fun findByEmail(email: String) =
            find { AccountTable.email eq email }

        /**
         * Find a user account by username
         */
        suspend fun findByName(name: String) =
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
        suspend fun create(name: String, inputPassword: String): EntityID<UInt> {
            logger.info { "Creating user account of $name" }
            CreateAccountEvent(name).broadcast()
            val hashSalt = generateSalt()
            val pwd = hashPassword(inputPassword, hashSalt)
            return AccountTable.insertAndGetId {
                it[userName] = name
                it[password] = pwd
            }
        }

        private suspend fun hashPassword(inputPassword: String, salt: String): String =
            Password.hash(inputPassword)
                .addSalt(salt)
                .apply {
                    if (usePepper) {
                        this.addPepper(pepper)
                    }
                }
                .with(argon2).result

        private fun generateSalt(): String =
            randomByteArray(DispatchConfig.data.accountSetting.password.saltByteLength).encodeBase64()
    }

    val userId by AccountTable.id
    var userName by AccountTable.userName
    private var password by AccountTable.password
    var email by AccountTable.email
    private var comboToken by AccountTable.comboToken
    private var comboTokenGenerationTime by AccountTable.comboTokenGenerationTime
    private var comboId by AccountTable.comboId
    private var dispatchToken by AccountTable.dispatchToken
    private var dispatchTokenGenerationTime by AccountTable.dispatchTokenGenerationTime
    var permissionLevel by AccountTable.permissionLevel

    internal suspend fun checkPassword(inputPassword: String): Boolean =
        Password.check(inputPassword, password)
            .apply {
                if (usePepper) {
                    this.addPepper(pepper)
                }
            }
            .with(argon2)

    @SorapointaInternal suspend fun updatePassword(inputPassword: String) {
        password = hashPassword(inputPassword, generateSalt())
    }

    internal suspend fun generateDipatchToken(): String {
        val token = randomByteArray(32).encodeBase64()
        dispatchToken = token
        dispatchTokenGenerationTime = now()
        return token
    }

    internal suspend fun generateComboToken(): String {
        val token = randomByteArray(20).hex
        comboToken = token
        comboTokenGenerationTime = now()
        return token
    }

    internal suspend fun getComboIdOrGenerate(): UInt =
        comboId ?: randomUInt().also {
            comboId = it
        }

    @SorapointaInternal suspend fun getComboToken(): String? {
        if (checkComboTokenExpire()) return null
        return comboToken
    }

    internal suspend fun getDispatchToken(): String? {
        if (checkDispatchTokenExpire()) return null
        return dispatchToken
    }

    private suspend fun checkComboTokenExpire(): Boolean =
        comboTokenGenerationTime?.let {
            now() - it > DispatchConfig.data.accountSetting.comboTokenExpiredTime
        } ?: true

    private suspend fun checkDispatchTokenExpire(): Boolean =
        dispatchTokenGenerationTime?.let {
            now() - it > DispatchConfig.data.accountSetting.dispatchTokenExpiredTime
        } ?: true
}
