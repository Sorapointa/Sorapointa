package org.sorapointa.dataloder

import mu.KotlinLogging
import org.junit.jupiter.api.Test
import org.sorapointa.dataloader.DataLoader
import org.sorapointa.dataloader.ResourceHolder
import org.sorapointa.dataloader.def.AvatarCostumeData
import org.sorapointa.utils.TestOption
import org.sorapointa.utils.runTest

class DataLoaderTest {
    private val avatarCostumeDataLoader =
        DataLoader<List<AvatarCostumeData>>("./ExcelBinOutput/AvatarCostumeExcelConfigData.json")

    val avatarCostumeDataList get() = avatarCostumeDataLoader.data

    @Test
    fun test() = runTest(TestOption.SKIP_CI) {
        ResourceHolder.loadAll()
        println(avatarCostumeDataList)
    }
}
