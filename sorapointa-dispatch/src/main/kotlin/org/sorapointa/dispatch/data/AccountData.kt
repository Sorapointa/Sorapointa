package org.sorapointa.dispatch.data

import com.password4j.Argon2Function
import com.password4j.Password
import io.ktor.util.*
import mu.KotlinLogging
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.insertAndGetId
import org.sorapointa.dispatch.DispatchConfig
import org.sorapointa.dispatch.events.CreateAccountEvent
import org.sorapointa.event.broadcast
import org.sorapointa.utils.crypto.randomByteArray
import org.sorapointa.utils.encoding.hex

private val logger = KotlinLogging.logger {}

object AccountTable : IdTable<UInt>("account_table") {
    override val id: Column<EntityID<UInt>> = uinteger("user_id").autoIncrement().entityId()
    val userName: Column<String> = varchar("user_name", 60).uniqueIndex()
    val password: Column<String> = varchar("password", 255)
    val email: Column<String?> = varchar("email", 255).uniqueIndex().nullable()
    val comboToken: Column<String?> = varchar("combo_token", 40).nullable()
    val comboId: Column<UInt?> = uinteger("combo_id").nullable()
    val dispatchToken: Column<String?> = varchar("dispatch_token", 32).nullable()
    val permissionLevel: Column<UShort> = ushort("permission_level").default(0u)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

@Suppress("RedundantSuspendModifier")
class Account(id: EntityID<UInt>) : Entity<UInt>(id) {

    companion object : EntityClass<UInt, Account>(AccountTable) {

        private val pepper = DispatchConfig.data.password.hashPepper

        val argon2: Argon2Function by lazy {
            val setting = DispatchConfig.data.password
            Argon2Function.getInstance(
                setting.memory,
                setting.iterations,
                setting.parallelism,
                setting.byteLength,
                setting.argon2Type,
                setting.argon2Version
            )
        }

        suspend fun findByEmail(email: String) =
            find { AccountTable.email eq email }

        suspend fun findByName(name: String) =
            find { AccountTable.userName eq name }

        suspend fun findOrCreate(name: String, inputPassword: String): Account =
            findByName(name).firstOrNull() ?: run { findById(create(name, inputPassword).value)!! }

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

        suspend fun hashPassword(inputPassword: String, salt: String): String =
            Password.hash(inputPassword)
                .addSalt(salt)
                .addPepper(pepper)
                .with(argon2).result

        fun generateSalt(): String =
            randomByteArray(DispatchConfig.data.password.saltByteLength).encodeBase64()
    }

    val userId by AccountTable.id
    var userName by AccountTable.userName
    var password by AccountTable.password
    var email by AccountTable.email
    var comboToken by AccountTable.comboToken
    var comboId by AccountTable.comboId
    var dispatchToken by AccountTable.dispatchToken
    var permissionLevel by AccountTable.permissionLevel

    suspend fun checkPassword(inputPassword: String): Boolean {
        return Password.check(inputPassword, password)
            .addPepper(pepper)
            .with(argon2)
    }

    suspend fun updatePassword(inputPassword: String) {
        password = hashPassword(inputPassword, generateSalt())
    }

    suspend fun getDispatchTokenOrGenerate(): String {
        val token = randomByteArray(32).encodeBase64()
        dispatchToken = token
        return token
    }

    suspend fun getComboTokenOrGenerate(): String {
        val token = randomByteArray(20).hex
        comboToken = token
        return token
    }

    suspend fun getComboIdOrGenerate(): UInt {
        val cid = (0..Int.MAX_VALUE).random().toUInt()
        comboId = cid
        return cid
    }
}
