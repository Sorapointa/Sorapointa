package org.sorapointa.dataloader.def

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.sorapointa.dataloader.DataLoader

private val codexWeaponDataLoader =
    DataLoader<List<CodexWeaponData>>("./ExcelBinOutput/WeaponCodexExcelConfigData.json")

val codexWeaponData get() = codexWeaponDataLoader.data

@Serializable
data class CodexWeaponData(
    @JsonNames("id", "Id")
    val id: Int,
    @JsonNames("weaponId", "WeaponId")
    val weaponId: Int,
    @JsonNames("sortOrder", "SortOrder")
    val sortOrder: Int
)
