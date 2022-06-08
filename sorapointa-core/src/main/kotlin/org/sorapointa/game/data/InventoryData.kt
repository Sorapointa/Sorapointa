package org.sorapointa.game.data

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.sorapointa.data.provider.sql.SetTable
import org.sorapointa.data.provider.sql.jsonb
import org.sorapointa.proto.MaterialDeleteInfoKt
import org.sorapointa.proto.MaterialDeleteInfoKt.countDownDelete
import org.sorapointa.proto.MaterialDeleteInfoKt.dateTimeDelete
import org.sorapointa.proto.MaterialDeleteInfoKt.delayWeekCountDownDelete
import org.sorapointa.proto.MaterialDeleteInfoOuterClass
import org.sorapointa.proto.materialDeleteInfo

internal object InventoryTable : SetTable<UInt, EntityID<ULong>>("inventory") {
    override val id: Column<EntityID<UInt>> =
        reference("player", PlayerDataTable, onDelete = ReferenceOption.CASCADE)
    override val value: Column<EntityID<ULong>> =
        reference("item_unique_id", ItemTable, onDelete = ReferenceOption.CASCADE)

    override val primaryKey: PrimaryKey = PrimaryKey(id, value)
}

internal object ItemTable : IdTable<ULong>("inventory_item") {
    override val id: Column<EntityID<ULong>> = ulong("item_unique_id").autoIncrement().entityId()

    val itemData: Column<ItemData> = jsonb("item_data")

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

class InventoryItem(id: EntityID<ULong>) : Entity<ULong>(id) {
    companion object : EntityClass<ULong, InventoryItem>(ItemTable)

    val item by ItemTable.itemData
}

@Serializable
sealed class ItemData {

    abstract val itemId: UInt
    abstract val guid: ULong

    @Serializable
    data class Material(
        override val itemId: UInt,
        override val guid: ULong,
        val count: UInt,
        val deleteInfo: MaterialDeleteInfo
    ) : ItemData()

    @Serializable
    data class Furniture(
        override val itemId: UInt,
        override val guid: ULong,
        val count: UInt
    ) : ItemData()

    @Serializable
    sealed class Equip : ItemData() {
        abstract val isLocked: Boolean

        @Serializable
        data class Weapon(
            override val itemId: UInt,
            override val guid: ULong,
            override val isLocked: Boolean,
            val level: UInt,
            val exp: UInt,
            val promoteLevel: UInt,
            val affixMap: Map<UInt, UInt>
        ) : Equip()

        @Serializable
        data class Reliquary(
            override val itemId: UInt,
            override val guid: ULong,
            override val isLocked: Boolean,
            val level: UInt,
            val exp: UInt,
            val promoteLevel: UInt,
            val mainPropId: UInt,
            val appendPropIdList: List<UInt>
        ) : Equip()
    }
}

@Serializable
sealed class MaterialDeleteInfo {

    abstract val hasDeleteConfig: Boolean

    fun toProto(): MaterialDeleteInfoOuterClass.MaterialDeleteInfo =
        materialDeleteInfo {
            hasDeleteConfig = this@MaterialDeleteInfo.hasDeleteConfig
            toProto()
        }

    abstract fun MaterialDeleteInfoKt.Dsl.toProto()

    @Serializable
    data class CountDownDelete(
        override val hasDeleteConfig: Boolean,
        val deleteTimeNumMap: Map<UInt, UInt>,
        val configCountDownTime: UInt
    ) : MaterialDeleteInfo() {

        override fun MaterialDeleteInfoKt.Dsl.toProto() {
            val t = this@CountDownDelete
            countDownDelete = countDownDelete {
                deleteTimeNumMap.putAll(t.deleteTimeNumMap.map { it.key.toInt() to it.value.toInt() }.toMap())
                configCountDownTime = t.configCountDownTime.toInt()
            }
        }
    }

    @Serializable
    data class DateTimeDelete(
        override val hasDeleteConfig: Boolean,
        val deleteTime: UInt
    ) : MaterialDeleteInfo() {

        override fun MaterialDeleteInfoKt.Dsl.toProto() {
            val t = this@DateTimeDelete
            dateDelete = dateTimeDelete {
                deleteTime = t.deleteTime.toInt()
            }
        }
    }

    @Serializable
    data class DelayWeekCountDownDelete(
        override val hasDeleteConfig: Boolean,
        val deleteTimeNumMap: Map<UInt, UInt>,
        val configCountDownTime: UInt,
        val configDelayWeek: UInt
    ) : MaterialDeleteInfo() {

        override fun MaterialDeleteInfoKt.Dsl.toProto() {
            val t = this@DelayWeekCountDownDelete
            delayWeekCountDownDelete = delayWeekCountDownDelete {
                deleteTimeNumMap.putAll(t.deleteTimeNumMap.map { it.key.toInt() to it.value.toInt() }.toMap())
                configCountDownTime = t.configCountDownTime.toInt()
                configDelayWeek = t.configDelayWeek.toInt()
            }
        }
    }
}
