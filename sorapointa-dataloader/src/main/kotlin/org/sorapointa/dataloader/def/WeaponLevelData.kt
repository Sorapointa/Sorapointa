package org.sorapointa.dataloader.def

import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val weaponLevelLoader =
    DataLoader<List<WeaponLevelData>>("./ExcelBinOutput/WeaponLevelExcelConfigData.json")

val weaponLevelData get() = weaponLevelLoader.data

@Serializable
data class WeaponLevelData(
    @JsonNames("level", "Level")
    val level: Int,
    @JsonNames("requiredExps", "RequiredExps")
    val requiredExps: List<Int>
)
