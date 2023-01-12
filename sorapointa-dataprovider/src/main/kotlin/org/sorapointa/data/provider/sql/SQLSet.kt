package org.sorapointa.data.provider.sql

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert

abstract class SetTable<TID : Comparable<TID>, V>(
    tableName: String
) : IdTable<TID>(tableName) {

    abstract val value: Column<V>
}

class SQLDatabaseSet<TID : Comparable<TID>, V>(
    id: EntityID<TID>,
    private val setTable: SetTable<TID, V>
) : CacheableData<TID>(id, setTable), MutableSet<V> {

    private val values
        get() = originalData.map { it[setTable.value] }

    override val size: Int
        get() = originalData.fetchSize ?: 0

    override fun clear() {
        needToUpdate()
        setTable.deleteWhere { setTable.id eq id }
    }

    override fun addAll(elements: Collection<V>): Boolean {
        needToUpdate()
        val previous = elements.intersect(values.toSet()).size != elements.size
        setTable.batchInsert(elements) {
            this[setTable.id] = this@SQLDatabaseSet.id
            this[setTable.value] = it
        }
        return previous
    }

    override fun add(element: V): Boolean {
        needToUpdate()
        val previous = !values.contains(element)
        setTable.insert {
            it[setTable.id] = this@SQLDatabaseSet.id
            it[setTable.value] = element
        }
        return previous
    }

    override fun isEmpty(): Boolean =
        originalData.empty()

    override fun iterator(): MutableIterator<V> =
        values.toMutableList().iterator()

    override fun retainAll(elements: Collection<V>): Boolean {
        needToUpdate()
        var isSet = false
        val removeList = arrayListOf<V>()
        values.forEach {
            if (!elements.contains(it)) {
                isSet = true
                removeList.add(it)
            }
        }
        removeAll(removeList)
        return isSet
    }

    override fun removeAll(elements: Collection<V>): Boolean {
        needToUpdate()
        val previous = elements.intersect(values.toSet()).isNotEmpty()
        elements.forEach {
            val e = setTable.value eq it
            setTable.deleteWhere { e }
        }
        return previous
    }

    override fun remove(element: V): Boolean {
        needToUpdate()
        val previous = values.contains(element)
        setTable.deleteWhere { setTable.value eq element }
        return previous
    }

    override fun containsAll(elements: Collection<V>): Boolean =
        elements.intersect(values.toSet()).size == elements.size

    override fun contains(element: V): Boolean =
        values.contains(element)

    override fun toString(): String =
        "SQLSet[tableName=${setTable.tableName},id=${id.value}," +
            "value=${values.joinToString(prefix = "[", postfix = "]")}]"
}
