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
import org.sorapointa.proto.Equip as ProtoEquip
import org.sorapointa.proto.MaterialDeleteInfo as ProtoMaterialDeleteInfo
import org.sorapointa.proto.MaterialDeleteInfo.CountDownDelete as ProtoCountDownDelete
import org.sorapointa.proto.MaterialDeleteInfo.DateTimeDelete as ProtoDateTimeDelete
import org.sorapointa.proto.MaterialDeleteInfo.DelayWeekCountDownDelete as ProtoDelayWeekCountDownDelete

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

    fun toProto(): Item {
        val t = this@ItemData
        return Item(item_id = t.itemId, guid = t.guid).toProto()
    }

    abstract fun Item.toProto(): Item

    @Serializable
    sealed class CountableItem : ItemData() {
        abstract val count: Int
    }

    @Serializable
    data class Material(
        override val itemId: Int,
        override val guid: Long,
        override val count: Int = 1,
        val deleteInfo: MaterialDeleteInfo? = null,
    ) : CountableItem() {

        val materialExcelData by lazy {
            materialData.firstOrNull { it.id == itemId } ?: error("Could not find materialId:$itemId data")
        }

        override fun Item.toProto(): Item {
            val t = this@Material
            val material = Material(
                count = t.count,
                delete_info = t.deleteInfo?.toProto(),
            )
            return copy(material = material)
        }
    }

    @Serializable
    data class Furniture(
        override val itemId: Int,
        override val guid: Long,
        override val count: Int = 1,
    ) : CountableItem() {

        val furnitureExcelData by lazy {
            materialData.firstOrNull { it.id == itemId } ?: error("Could not find materialId:$itemId data")
        }

        override fun Item.toProto(): Item =
            copy(furniture = Furniture(count = this@Furniture.count))
    }

    @Serializable
    sealed class Equip : ItemData() {
        abstract val isLocked: Boolean

        override fun Item.toProto(): Item =
            copy(equip = ProtoEquip(is_locked = isLocked))

        abstract fun ProtoEquip.toProto(): ProtoEquip

        @Serializable
        data class Weapon(
            override val itemId: Int,
            override val guid: Long,
            override val isLocked: Boolean = false,
            val level: Int = 1,
            val exp: Int = 0,
            val promoteLevel: Int = 0,
            // Refinement level starts from [0, 4], null for 1-2 star weapons
            val refinement: Int? = null,
        ) : Equip() {

            val weaponExcelData by lazy {
                weaponData.firstOrNull { it.id == itemId } ?: error("Could not find weaponId:$itemId data")
            }

            // itemId: 15509 -> affixId: 115509
            @kotlinx.serialization.Transient
            private val refinementAffixId = itemId + 100000

            override fun ProtoEquip.toProto(): ProtoEquip =
                copy(
                    weapon = Weapon(
                        level = level,
                        exp = exp,
                        promote_level = promoteLevel,
                        affix_map = if (refinement != null) {
                            mapOf(refinementAffixId to refinement)
                        } else {
                            emptyMap()
                        },
                    ),
                )

            fun toSceneWeaponInfoProto(entityId: Int) =
                SceneWeaponInfo(
                    entity_id = entityId,
                    gadget_id = weaponExcelData.gadgetId,
                    item_id = itemId,
                    guid = guid,
                    level = level,
                    promote_level = promoteLevel,
                    affix_map = if (refinement != null) {
                        mapOf(refinementAffixId to refinement)
                    } else {
                        emptyMap()
                    },
                    // TODO: Unknown
                    ability_info = AbilitySyncStateInfo(is_inited = true),
                    // rendererChangedInfo
                )
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
            val appendPropIdList: List<Int> = listOf(),
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
                        },
                )

            override fun ProtoEquip.toProto(): ProtoEquip =
                copy(
                    reliquary = Reliquary(
                        level = level,
                        exp = exp,
                        promote_level = promoteLevel,
                        main_prop_id = mainPropId,
                        append_prop_id_list = appendPropIdList,
                    ),
                )

            fun toSceneReliquaryInfoProto() = SceneReliquaryInfo(
                item_id = itemId,
                guid = guid,
                level = level,
                promote_level = promoteLevel,
            )
        }
    }
}

@Serializable
sealed class MaterialDeleteInfo {

    abstract val hasDeleteConfig: Boolean

    fun toProto(): ProtoMaterialDeleteInfo =
        ProtoMaterialDeleteInfo(
            has_delete_config = hasDeleteConfig,
        ).toProto()

    abstract fun ProtoMaterialDeleteInfo.toProto(): ProtoMaterialDeleteInfo

    @Serializable
    data class CountDownDelete(
        override val hasDeleteConfig: Boolean,
        val deleteTimeNumMap: Map<Int, Int>,
        val configCountDownTime: Int,
    ) : MaterialDeleteInfo() {

        override fun ProtoMaterialDeleteInfo.toProto(): ProtoMaterialDeleteInfo = copy(
            count_down_delete = ProtoCountDownDelete(
                delete_time_num_map = deleteTimeNumMap,
                config_count_down_time = configCountDownTime,
            ),
        )
    }

    @Serializable
    data class DateTimeDelete(
        override val hasDeleteConfig: Boolean,
        val deleteTime: Int,
    ) : MaterialDeleteInfo() {

        override fun ProtoMaterialDeleteInfo.toProto(): ProtoMaterialDeleteInfo = copy(
            date_delete = ProtoDateTimeDelete(
                delete_time = deleteTime,
            ),
        )
    }

    @Serializable
    data class DelayWeekCountDownDelete(
        override val hasDeleteConfig: Boolean,
        val deleteTimeNumMap: Map<Int, Int>,
        val configCountDownTime: Int,
        val configDelayWeek: Int,
    ) : MaterialDeleteInfo() {

        override fun ProtoMaterialDeleteInfo.toProto(): ProtoMaterialDeleteInfo = copy(
            delay_week_count_down_delete = ProtoDelayWeekCountDownDelete(
                delete_time_num_map = deleteTimeNumMap,
                config_count_down_time = configCountDownTime,
                config_delay_week = configDelayWeek,
            ),
        )
    }
}
