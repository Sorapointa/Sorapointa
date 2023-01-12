package org.sorapointa.game

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.getAndUpdate
import org.sorapointa.dataloader.def.findReliquaryExcelData
import org.sorapointa.dataloader.def.materialData
import org.sorapointa.dataloader.def.reliquaryData
import org.sorapointa.dataloader.def.weaponData
import org.sorapointa.events.PlayerLoginEvent
import org.sorapointa.proto.*
import org.sorapointa.proto.AbilitySyncStateInfo
import org.sorapointa.proto.Equip
import org.sorapointa.proto.Item
import org.sorapointa.proto.SceneReliquaryInfo
import org.sorapointa.proto.SceneWeaponInfo
import org.sorapointa.proto.bin.*
import org.sorapointa.server.network.PlayerStoreNotifyPacket
import org.sorapointa.server.network.StoreWeightLimitNotifyPacket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import org.sorapointa.proto.Item as ItemProto
import org.sorapointa.proto.Material as MaterialProto
import org.sorapointa.proto.Reliquary as ReliquaryProto
import org.sorapointa.proto.Weapon as WeaponProto
import org.sorapointa.proto.Furniture as FurnitureProto

class PlayerItemComp(
    override val player: Player,
    private val initPlayerItemBin: PlayerItemCompBin
) : PlayerModule {

    companion object {

        internal fun createNew(): PlayerItemCompBin {
            return PlayerItemCompBin(
                pack_store = ItemStoreBin()
            )
        }
    }

    internal fun init() {
        val player = player.impl()
        player.registerEventBlockListener<PlayerLoginEvent> {
            player.sendPacket(StoreWeightLimitNotifyPacket())
            player.sendPacket(PlayerStoreNotifyPacket(player))
        }
    }

    val packStore = PlayerStore(this, initPlayerItemBin.pack_store ?: ItemStoreBin())
    val primoGem = initPlayerItemBin.hcoin
    val mora = initPlayerItemBin.scoin
    private val itemCdMap = ConcurrentHashMap(initPlayerItemBin.item_cd_map)
    val resinRecord = initPlayerItemBin.resin_record
    val genesisCrystal = initPlayerItemBin.mcoin
    val legendaryKey = initPlayerItemBin.legendary_key
    val homeCoin = initPlayerItemBin.home_coin

    fun toBin(): PlayerItemCompBin {
        return PlayerItemCompBin(
            pack_store = packStore.toBin(),
            hcoin = primoGem,
            scoin = mora,
            item_cd_map = itemCdMap,
            resin_record = resinRecord,
            mcoin = genesisCrystal,
            legendary_key = legendaryKey,
            home_coin = homeCoin
        )
    }
}

class PlayerStore(
    val playerItemCompModule: PlayerItemComp,
    private val initStoreBin: ItemStoreBin
) {

    private val itemList = initStoreBin.item_list
        .associate { it.guid to AbstractItem.buildItemModule(it) }
        .toMutableMap()
    val isMaterialItemNumAlarmed = initStoreBin.is_material_item_num_alarmed // unk

    private val basicModule = playerItemCompModule.player.basicComp

    fun findItem(guid: Long) =
        itemList[guid] ?: error("Can find item $guid")

    fun findItemOrNull(guid: Long) =
        itemList[guid]

    fun findItem(itemId: Int) =
        itemList.values.first { it.itemId == itemId }

    fun findItemOrNull(itemId: Int) =
        itemList.values.firstOrNull { it.itemId == itemId }

    fun removeItem(guid: Long) =
        itemList.remove(guid)

    fun removeItem(
        itemGuid: Long,
        count: Int,
        forceRemove: Boolean
    ): Boolean {
        val item = findItemOrNull(itemGuid) ?: return false
        if (item is CountableItem) {
            if (item.count < count) {
                if (forceRemove) {
                    removeItem(itemGuid)
                    return true
                }
                return false
            } else if (item.count == count) {
                removeItem(itemGuid)
                return true
            }
            item.getAndUpdateCount { it - count }
            return true
        } else {
            return removeItem(itemGuid) != null
        }
    }

    fun removeItem(
        itemId: Int,
        count: Int,
        forceRemove: Boolean
    ): Boolean {
        val item = findItemOrNull(itemId) ?: return false
        return removeItem(item.guid, count, forceRemove)
    }

    fun addItem(item: AbstractItem) {
        itemList[item.guid] = item
    }

    fun addItem(itemBin: ItemBin) {
        itemList[itemBin.guid] = AbstractItem.buildItemModule(itemBin)
    }

    fun addItem(itemList: List<ItemBin>) {
        itemList.forEach { addItem(it) }
    }

    fun containsItem(itemId: Int) =
        itemList.values.any { it.itemId == itemId }

    fun containsItem(guid: Long) =
        itemList.containsKey(guid)

    fun createMaterial(
        itemId: Int,
        count: Int = 1,
    ): MaterialItem {
        return MaterialItem(
            itemId = itemId,
            count = count,
            guid = basicModule.getNextGuid(),
        )
    }

    fun createReliquaryItem(
        itemId: Int,
        isLocked: Boolean = false,
        level: Int = 1,
        exp: Int = 0,
        promoteLevel: Int = 0,
        appendPropIdList: List<Int> = listOf()
    ): ReliquaryItem {
        return ReliquaryItem(
            itemId = itemId,
            guid = basicModule.getNextGuid(),
            isLocked = isLocked,
            level = level,
            exp = exp,
            mainPropId = findReliquaryExcelData(itemId).mainProp.id,
            promoteLevel = promoteLevel,
            appendPropIdList = appendPropIdList,
        )
    }

    fun createWeaponItem(
        itemId: Int,
        isLocked: Boolean = false,
        level: Int = 1,
        exp: Int = 0,
        promoteLevel: Int = 0,
        refinementLevel: Int? = null,
    ): WeaponItem {
        return WeaponItem(
            itemId = itemId,
            guid = basicModule.getNextGuid(),
            isLocked = isLocked,
            level = level,
            exp = exp,
            promoteLevel = promoteLevel,
            refinementLevel = refinementLevel,
        )
    }

    fun createFurnitureItem(
        itemId: Int,
        count: Int
    ): FurnitureItem {
        return FurnitureItem(
            itemId = itemId,
            guid = basicModule.getNextGuid(),
            count = count,
        )
    }

    fun getAllItems() = itemList.values.toList()

    internal fun toBin(): ItemStoreBin {
        return ItemStoreBin(
            item_list = itemList.values.map { it.toBin() },
            is_material_item_num_alarmed = isMaterialItemNumAlarmed
        )
    }
}

interface CountableItem {

    val count: Int

    fun getAndUpdateCount(update: (Int) -> (Int)): Int
}

abstract class AbstractItem(
    val itemType: ItemType,
    val itemId: Int,
    val guid: Long,
//    protected val initItemBin: ItemBin
) {

    companion object {

        internal fun buildItemModule(
            itemBin: ItemBin
        ): AbstractItem {
            return when (itemBin.item_type) {
                ItemType.ITEM_MATERIAL -> MaterialItem.buildMaterialModule(itemBin)
                ItemType.ITEM_EQUIP -> {
                    itemBin.equip?.let {
                        if (it.reliquary != null) {
                            ReliquaryItem.buildReliquaryModule(itemBin)
                        } else if (it.weapon != null) {
                            WeaponItem.buildWeaponModule(itemBin)
                        } else error("Unknown equip type")
                    } ?: error("Not a equip item")
                }
                ItemType.ITEM_FURNITURE -> FurnitureItem.buildFurnitureModule(itemBin)
                else -> error("Unknown item type")
            }
        }
    }

    internal open fun toBin(): ItemBin {
        return ItemBin(
            item_type = itemType,
            item_id = itemId,
            guid = guid,
        )
    }

    internal open fun toProto(): ItemProto {
        return ItemProto(
            item_id = itemId,
            guid = guid,
        )
    }
}

@Suppress("MemberVisibilityCanBePrivate")
class MaterialItem(
    itemId: Int,
    guid: Long,
    count: Int,
) : AbstractItem(ItemType.ITEM_MATERIAL, itemId, guid), CountableItem {

    companion object {

        fun buildMaterialModule(initItemBin: ItemBin): MaterialItem {
            check(initItemBin.item_type == ItemType.ITEM_MATERIAL)
            return MaterialItem(
                initItemBin.item_id,
                initItemBin.guid,
                initItemBin.material?.count ?: 0
            )
        }
    }

    override val count
        get() = _count.value

    private val _count = atomic(count)

    val excelData by lazy {
        materialData.firstOrNull { it.id == itemId } ?: error("Could not find materialId:$itemId data")
    }

    override fun toBin(): ItemBin {
        return super.toBin().copy(
            material = MaterialBin(
                count = count
            )
        )
    }

    override fun toProto(): ItemProto {
        return super.toProto().copy(
            material = MaterialProto(
                count = count
            )
        )
    }

    override fun getAndUpdateCount(update: (Int) -> (Int)): Int {
        return _count.getAndUpdate { update(it) }
    }

    override fun toString(): String =
        "MaterialItem[itemId: $itemId, guid: $guid, count: $count]"
}

abstract class AbstractEquip(
    itemId: Int,
    guid: Long,
    isLocked: Boolean
) : AbstractItem(ItemType.ITEM_EQUIP, itemId, guid) {

    val isLocked
        get() = _isLocked.value

    private val _isLocked = atomic(isLocked)

    override fun toBin(): ItemBin {
        return super.toBin().copy(
            equip = EquipBin(
                is_locked = isLocked,
            )
        )
    }

    override fun toProto(): Item {
        return super.toProto().copy(
            equip = Equip(
                is_locked = isLocked,
            )
        )
    }
}

@Suppress("MemberVisibilityCanBePrivate")
class ReliquaryItem(
    itemId: Int,
    guid: Long,
    isLocked: Boolean,
    level: Int,
    exp: Int,
    val promoteLevel: Int,
    val mainPropId: Int,
    appendPropIdList: List<Int>
) : AbstractEquip(itemId, guid, isLocked) {

    companion object {

        fun buildReliquaryModule(initItemBin: ItemBin): ReliquaryItem {
            check(initItemBin.item_type == ItemType.ITEM_EQUIP)
            check(initItemBin.equip?.reliquary != null)
            val equipBin = initItemBin.equip!!
            val reliquaryBin = equipBin.reliquary!!
            return ReliquaryItem(
                initItemBin.item_id,
                initItemBin.guid,
                equipBin.is_locked,
                reliquaryBin.level,
                reliquaryBin.exp,
                reliquaryBin.main_prop_id,
                reliquaryBin.promote_level,
                reliquaryBin.append_prop_id_list
            )
        }
    }

    val level
        get() = _level.value

    val exp
        get() = _exp.value

    private val appendPropIdList = ConcurrentLinkedQueue(appendPropIdList)

    val excelData by lazy {
        reliquaryData.firstOrNull { it.id == itemId } ?: error("Could not find reliquaryId:$itemId data")
    }

    private val _level = atomic(level)
    private val _exp = atomic(exp)

    fun addRandomAppendProp(count: Int = excelData.appendPropNum) {
        repeat(count) {
            appendPropIdList.add(excelData.getRandomAppendProps().id)
        }
    }

    override fun toBin(): ItemBin {
        return super.toBin().copy(
            equip = super.toBin().equip!!.copy(
                reliquary = ReliquaryBin(
                    level = level,
                    exp = exp,
                    promote_level = promoteLevel,
                    main_prop_id = mainPropId,
                    append_prop_id_list = appendPropIdList.toList(),
                )
            )
        )
    }

    override fun toProto(): Item {
        return super.toProto().copy(
            equip = super.toProto().equip?.copy(
                reliquary = ReliquaryProto(
                    level = level,
                    exp = exp,
                    promote_level = promoteLevel,
                    main_prop_id = mainPropId,
                    append_prop_id_list = appendPropIdList.toList(),
                )
            )
        )
    }

    fun toSceneReliquaryInfoProto() = SceneReliquaryInfo(
        item_id = itemId,
        guid = guid,
        level = level,
        promote_level = promoteLevel,
    )

    override fun toString(): String =
        "ReliquaryItem[itemId: $itemId, guid: $guid, level: $level, " +
            "exp: $exp, mainPropId: $mainPropId, promoteLevel: $promoteLevel appendPropIdList: $appendPropIdList]"
}

@Suppress("MemberVisibilityCanBePrivate")
class WeaponItem(
    itemId: Int,
    guid: Long,
    isLocked: Boolean,
    level: Int,
    exp: Int,
    promoteLevel: Int,
    refinementLevel: Int? = null
) : AbstractEquip(itemId, guid, isLocked) {

    companion object {

        fun getRefinementId(itemId: Int) = itemId + 1000000

        fun buildWeaponModule(initItemBin: ItemBin): WeaponItem {
            check(initItemBin.item_type == ItemType.ITEM_EQUIP)
            check(initItemBin.equip?.weapon != null)
            val equipBin = initItemBin.equip!!
            val weaponBin = equipBin.weapon!!
            return WeaponItem(
                initItemBin.item_id,
                initItemBin.guid,
                equipBin.is_locked,
                weaponBin.level,
                weaponBin.exp,
                weaponBin.promote_level,
                weaponBin.affix_map[getRefinementId(initItemBin.item_id)]
            )
        }
    }

    val level
        get() = _level.value

    val exp
        get() = _exp.value

    val promoteLevel
        get() = _promoteLevel.value

    private val refinementAffixId = getRefinementId(itemId)
    val refinementLevel
        get() = _refinementLevel.value

    val excelData by lazy {
        weaponData.firstOrNull { it.id == itemId } ?: error("Could not find weaponId:$itemId data")
    }

    private val _level = atomic(level)
    private val _exp = atomic(exp)
    private val _promoteLevel = atomic(promoteLevel)
    private val _refinementLevel = atomic(refinementLevel)

    private val affixMap
        get() = refinementLevel?.let {
            mapOf(refinementAffixId to it)
        } ?: mapOf()

    override fun toBin(): ItemBin {
        return super.toBin().copy(
            equip = super.toBin().equip?.copy(
                weapon = WeaponBin(
                    level = level,
                    exp = exp,
                    promote_level = promoteLevel,
                    affix_map = affixMap,
                )
            )
        )
    }

    override fun toProto(): Item {
        return super.toProto().copy(
            equip = super.toProto().equip?.copy(
                weapon = WeaponProto(
                    level = level,
                    exp = exp,
                    promote_level = promoteLevel,
                    affix_map = affixMap
                )
            )
        )
    }

    fun toSceneWeaponInfoProto(entityId: Int) =
        SceneWeaponInfo(
            entity_id = entityId,
            gadget_id = excelData.gadgetId,
            item_id = itemId,
            guid = guid,
            level = level,
            promote_level = promoteLevel,
            affix_map = affixMap,
            // TODO: Unknown
            ability_info = AbilitySyncStateInfo(is_inited = true),
            // rendererChangedInfo
        )

    override fun toString(): String =
        "WeaponItem[itemId: $itemId, guid: $guid, level: $level, " +
            "exp: $exp, promoteLevel: $promoteLevel, refinementLevel: $refinementLevel]"
}

class FurnitureItem(
    itemId: Int,
    guid: Long,
    count: Int,
) : AbstractItem(ItemType.ITEM_FURNITURE, itemId, guid), CountableItem {

    companion object {

        fun buildFurnitureModule(initItemBin: ItemBin): FurnitureItem {
            check(initItemBin.item_type == ItemType.ITEM_FURNITURE)
            return FurnitureItem(
                initItemBin.item_id,
                initItemBin.guid,
                initItemBin.furniture?.count ?: 0,
            )
        }
    }

    override val count
        get() = _count.value

    private val _count = atomic(count)

    override fun toBin(): ItemBin {
        return super.toBin().copy(
            furniture = FurnitureBin(
                count = count,
            )
        )
    }

    override fun toProto(): Item {
        return super.toProto().copy(
            furniture = FurnitureProto(
                count = count,
            )
        )
    }

    override fun getAndUpdateCount(update: (Int) -> (Int)): Int {
        return _count.getAndUpdate { update(it) }
    }

    override fun toString(): String =
        "FurnitureItem[itemId: $itemId, guid: $guid, count: $count]"
}
