package org.sorapointa.game.data

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

object SorapointaStoreTable : IdTable<String>("sorapointa_store") {

    override val id: Column<EntityID<String>> = varchar("key", 255)
        .entityId()
    val value: Column<String> = varchar("value", 255)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

object SorapointaStore {

//    private const val GUID_COUNTER_KEY = "guidCounter"

    suspend fun initDefaultEntry() {
        newSuspendedTransaction {
//            initEntry(GUID_COUNTER_KEY, "0")
        }
    }

//    fun getAndIncGuidCounter(): ULong =
//        getKeyAndUpdate(GUID_COUNTER_KEY) { it.toULong().inc() }

    private fun initEntry(key: String, defaultValue: String) {
        if (findKey(key).count() == 0L) {
            SorapointaStoreTable.insert {
                it[id] = key
                it[value] = defaultValue
            }
        }
    }

    private fun findKey(key: String) =
        SorapointaStoreTable.select { SorapointaStoreTable.id eq key }

    private inline fun <T> getKeyAndUpdate(key: String, crossinline update: (String) -> T): T {
        val v = findKey(key).firstOrNull()?.let {
            it[SorapointaStoreTable.value]
        } ?: error("$key haven't init properly")

        val newValue = update(v)
        SorapointaStoreTable.update {
            it[value] = newValue.toString()
        }
        return newValue
    }
}
