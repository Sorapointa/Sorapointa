package org.sorapointa.game

import org.sorapointa.dataloader.def.findReliquaryExcelData
import org.sorapointa.game.data.ItemData
import org.sorapointa.game.data.MaterialDeleteInfo

interface Inventory {

    fun findItem(guid: Long): ItemData?

    fun findItemById(itemId: Int): ItemData?

    fun putItem(itemDate: ItemData)

    fun createMaterialItem(
        itemId: Int,
        count: Int = 1,
        deleteInfo: MaterialDeleteInfo? = null
    ): ItemData.Material

    fun createFurnitureItem(
        itemId: Int,
        count: Int = 1
    ): ItemData.Furniture

    fun createWeaponItem(
        itemId: Int,
        isLocked: Boolean = false,
        level: Int = 1,
        exp: Int = 0,
        promoteLevel: Int = 0,
        refinementLevel: Int? = null
    ): ItemData.Equip.Weapon

    fun createReliquaryItem(
        itemId: Int,
        isLocked: Boolean = false,
        level: Int = 1,
        exp: Int = 0,
        appendPropIdList: List<Int> = listOf()
    ): ItemData.Equip.Reliquary

    fun removeItem(itemGuid: Long): ItemData?

    fun removeItem(
        itemGuid: Long,
        count: Int = 1,
        forceRemove: Boolean = false
    ): Boolean

    fun removeItemById(
        itemId: Int,
        count: Int = 1,
        forceRemove: Boolean = false
    ): Boolean

}

class InventoryImpl(
    private val guidEntity: GuidEntity,
    private val data: MutableMap<Long, ItemData>,
): Inventory {

    override fun findItem(guid: Long) =
        data[guid]

    override fun findItemById(itemId: Int): ItemData? =
        data.values.firstOrNull { it.itemId == itemId }

    override fun putItem(itemDate: ItemData) {
        data[itemDate.guid] = itemDate
    }

    override fun createMaterialItem(
        itemId: Int,
        count: Int,
        deleteInfo: MaterialDeleteInfo?
    ) = ItemData.Material(
        itemId = itemId,
        guid = guidEntity.getNextGuid(),
        count = count,
        deleteInfo = deleteInfo
    )

    override fun createFurnitureItem(
        itemId: Int,
        count: Int
    ) = ItemData.Furniture(
        itemId = itemId,
        guid = guidEntity.getNextGuid(),
        count = count
    )

    override fun createWeaponItem(
        itemId: Int,
        isLocked: Boolean,
        level: Int,
        exp: Int,
        promoteLevel: Int,
        refinementLevel: Int?
    ) = ItemData.Equip.Weapon(
        itemId = itemId,
        guid = guidEntity.getNextGuid(),
        isLocked = isLocked,
        level = level,
        exp = exp,
        promoteLevel = promoteLevel,
        refinement = refinementLevel
    )

    override fun createReliquaryItem(
        itemId: Int,
        isLocked: Boolean,
        level: Int,
        exp: Int,
        appendPropIdList: List<Int>
    ) = ItemData.Equip.Reliquary(
        itemId = itemId,
        guid = guidEntity.getNextGuid(),
        isLocked = isLocked,
        level = level,
        exp = exp,
        mainPropId = findReliquaryExcelData(itemId).mainProp.id,
        appendPropIdList = appendPropIdList
    )

    override fun removeItem(itemGuid: Long) =
        data.remove(itemGuid)

    override fun removeItem(
        itemGuid: Long,
        count: Int,
        forceRemove: Boolean
    ): Boolean {
        val item = data[itemGuid] ?: return false
        return when (item) {
            is ItemData.CountableItem -> {
                when {
                    item.count > count -> {
                        data[itemGuid] = when (item) {
                            is ItemData.Material -> item.copy(count = item.count - count)
                            is ItemData.Furniture -> item.copy(count = item.count - count)
                        }
                        true
                    }
                    item.count == count -> {
                        removeItem(item.guid)
                        true
                    }
                    else -> {
                        if (forceRemove) removeItem(item.guid)
                        false
                    }
                }
            }
            is ItemData.Equip -> {
                removeItem(item.guid)
                true
            }
        }
    }

    override fun removeItemById(
        itemId: Int,
        count: Int,
        forceRemove: Boolean
    ): Boolean {
        val item = findItemById(itemId) ?: return false
        return removeItem(item.guid, count, forceRemove)
    }

    // TODO: Virtual Item Handle

}


