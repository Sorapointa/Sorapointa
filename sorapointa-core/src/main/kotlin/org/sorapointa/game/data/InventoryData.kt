package org.sorapointa.game.data

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.sorapointa.data.provider.sql.MapTable
import org.sorapointa.data.provider.sql.jsonb
import org.sorapointa.dataloader.def.materialData
import org.sorapointa.dataloader.def.reliquaryData
import org.sorapointa.dataloader.def.weaponData
import org.sorapointa.proto.*
import org.sorapointa.proto.ItemOuterClass.Item
import org.sorapointa.proto.MaterialDeleteInfoKt.countDownDelete
import org.sorapointa.proto.MaterialDeleteInfoKt.dateTimeDelete
import org.sorapointa.proto.MaterialDeleteInfoKt.delayWeekCountDownDelete

internal object InventoryTable : MapTable<Int, Long, ItemData>("inventory") {
    override val id: Column<EntityID<Int>> =
        reference("player", PlayerDataTable, onDelete = ReferenceOption.CASCADE)

    override val key: Column<Long> = long("item_guid").uniqueIndex()

    override val value: Column<ItemData> = jsonb("item_data")

    override val primaryKey: Table.PrimaryKey = PrimaryKey(id, key)
}

@Serializable
sealed class ItemData {

    abstract val itemId: Int
    abstract val guid: Long

    fun toProto(): Item =
        item {
            val t = this@ItemData
            itemId = t.itemId
            guid = t.guid
            toProto()
        }

    abstract fun ItemKt.Dsl.toProto()

    @Serializable
    sealed class CountableItem : ItemData() {

        abstract val count: Int
    }

    @Serializable
    data class Material(
        override val itemId: Int,
        override val guid: Long,
        override val count: Int = 1,
        val deleteInfo: MaterialDeleteInfo? = null
    ) : CountableItem() {

        val materialExcelData by lazy {
            materialData.firstOrNull { it.id == itemId } ?: error("Could not find materialId:$itemId data")
        }

        override fun ItemKt.Dsl.toProto() {
            val t = this@Material
            material = material {
                count = t.count
                t.deleteInfo?.let { deleteInfo = it.toProto() }
            }
        }
    }

    @Serializable
    data class Furniture(
        override val itemId: Int,
        override val guid: Long,
        override val count: Int = 1
    ) : CountableItem() {

        val furnitureExcelData by lazy {
            materialData.firstOrNull { it.id == itemId } ?: error("Could not find materialId:$itemId data")
        }

        override fun ItemKt.Dsl.toProto() {
            furniture = furniture {
                count = this@Furniture.count
            }
        }
    }

    @Serializable
    sealed class Equip : ItemData() {
        abstract val isLocked: Boolean

        override fun ItemKt.Dsl.toProto() {
            equip = equip {
                isLocked = this@Equip.isLocked
                toProto()
            }
        }

        abstract fun EquipKt.Dsl.toProto()

        @Serializable
        data class Weapon(
            override val itemId: Int,
            override val guid: Long,
            override val isLocked: Boolean = false,
            val level: Int = 1,
            val exp: Int = 0,
            val promoteLevel: Int = 0,
            // Refinement level starts from [0, 4], null for 1-2 star weapons
            val refinement: Int? = null
        ) : Equip() {

            val weaponExcelData by lazy {
                weaponData.firstOrNull { it.id == itemId } ?: error("Could not find weaponId:$itemId data")
            }

            // itemId: 15509 -> affixId: 115509
            @kotlinx.serialization.Transient
            private val refinementAffixId = itemId + 100000

            override fun EquipKt.Dsl.toProto() {
                val t = this@Weapon
                weapon = weapon {
                    level = t.level
                    exp = t.exp
                    promoteLevel = t.promoteLevel
                    refinement?.let { affixMap.put(refinementAffixId, it) }
                }
            }

            fun toSceneWeaponInfoProto(entityId: Int) =
                sceneWeaponInfo {
                    this.entityId = entityId
                    gadgetId = weaponExcelData.gadgetId
                    itemId = this@Weapon.itemId
                    guid = this@Weapon.guid
                    level = this@Weapon.level
                    promoteLevel = this@Weapon.promoteLevel
                    refinement?.let { affixMap.put(refinementAffixId, it) }
                    // TODO: Unknown
                    abilityInfo = abilitySyncStateInfo { isInited = true }
                    // rendererChangedInfo
                }
        }

        @Serializable
        data class Reliquary(
            override val itemId: Int,
            override val guid: Long,
            override val isLocked: Boolean = false,
            val level: Int = 1,
            val exp: Int = 0,
            val promoteLevel: Int = 0,
            val mainPropId: Int,
            val appendPropIdList: List<Int> = listOf()
        ) : Equip() {

            val reliquaryExcelData by lazy {
                reliquaryData.firstOrNull { it.id == itemId } ?: error("Could not find reliquaryId:$itemId data")
            }

            fun copyOfNewRandomProp(count: Int = reliquaryExcelData.appendPropNum) =
                copy(
                    appendPropIdList = appendPropIdList
                        .toMutableList()
                        .apply {
                            repeat(count) {
                                add(reliquaryExcelData.getRandomAppendProps().id)
                            }
                        }
                )

            override fun EquipKt.Dsl.toProto() {
                val t = this@Reliquary
                reliquary = reliquary {
                    level = t.level
                    exp = t.exp
                    promoteLevel = t.promoteLevel
                    mainPropId = t.mainPropId
                    appendPropIdList.addAll(t.appendPropIdList)
                }
            }

            fun toSceneReliquaryInfoProto() =
                sceneReliquaryInfo {
                    itemId = this@Reliquary.itemId
                    guid = this@Reliquary.guid
                    level = this@Reliquary.level
                    promoteLevel = this@Reliquary.promoteLevel
                }
        }
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
        val deleteTimeNumMap: Map<Int, Int>,
        val configCountDownTime: Int
    ) : MaterialDeleteInfo() {

        override fun MaterialDeleteInfoKt.Dsl.toProto() {
            val t = this@CountDownDelete
            countDownDelete = countDownDelete {
                deleteTimeNumMap.putAll(t.deleteTimeNumMap)
                configCountDownTime = t.configCountDownTime
            }
        }
    }

    @Serializable
    data class DateTimeDelete(
        override val hasDeleteConfig: Boolean,
        val deleteTime: Int
    ) : MaterialDeleteInfo() {

        override fun MaterialDeleteInfoKt.Dsl.toProto() {
            val t = this@DateTimeDelete
            dateDelete = dateTimeDelete {
                deleteTime = t.deleteTime
            }
        }
    }

    @Serializable
    data class DelayWeekCountDownDelete(
        override val hasDeleteConfig: Boolean,
        val deleteTimeNumMap: Map<Int, Int>,
        val configCountDownTime: Int,
        val configDelayWeek: Int
    ) : MaterialDeleteInfo() {

        override fun MaterialDeleteInfoKt.Dsl.toProto() {
            val t = this@DelayWeekCountDownDelete
            delayWeekCountDownDelete = delayWeekCountDownDelete {
                deleteTimeNumMap.putAll(t.deleteTimeNumMap)
                configCountDownTime = t.configCountDownTime
                configDelayWeek = t.configDelayWeek
            }
        }
    }
}
