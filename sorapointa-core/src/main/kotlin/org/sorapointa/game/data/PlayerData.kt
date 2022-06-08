package org.sorapointa.game.data

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*
import java.util.*

internal object PlayerDataTable : IdTable<UInt>("player_data") {
    const val LOCALE_LENGTH_LIMIT = 20

    override val id: Column<EntityID<UInt>> = uinteger("user_id").entityId()
    val guid: Column<ULong> = ulong("guid").default(0uL)
    val locale: Column<String?> = varchar("locale", LOCALE_LENGTH_LIMIT).nullable()

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

@Suppress("RedundantSuspendModifier")
class PlayerData(id: EntityID<UInt>) : Entity<UInt>(id) {
    companion object : EntityClass<UInt, PlayerData>(PlayerDataTable) {

        suspend fun findOrCreate(id: UInt): PlayerData =
            findById(id) ?: run { new(id = id) {} }
    }

    var guid by PlayerDataTable.guid
    var locale: Locale by PlayerDataTable.locale.transform(
        toColumn = { it.toLanguageTag() },
        toReal = { Locale.forLanguageTag(it) }
    )

    val inventory by InventoryItem via InventoryTable

    fun getNextGuid(): ULong {
        val nextGuid = ++guid
        return (id.value.toULong() shl 32) + nextGuid
    }
}
