package org.sorapointa.config

import org.jetbrains.exposed.sql.Table
import org.sorapointa.SorapointaConfig
import org.sorapointa.data.provider.DataFilePersist
import org.sorapointa.data.provider.DatabaseConfig
import org.sorapointa.dispatch.DispatchConfig
import org.sorapointa.dispatch.data.AccountTable
import org.sorapointa.event.EventManagerConfig
import org.sorapointa.game.data.InventoryTable
import org.sorapointa.game.data.ItemTable
import org.sorapointa.game.data.PlayerDataTable
import org.sorapointa.task.CronTasks
import org.sorapointa.utils.I18nConfig

internal val registeredConfig: List<DataFilePersist<*>> = listOf(
    DatabaseConfig,
    SorapointaConfig,
    I18nConfig,
    EventManagerConfig,
    DispatchConfig,
)

internal val registeredDatabaseTable: List<Table> = listOf(
    CronTasks,
    AccountTable,
    PlayerDataTable,
    InventoryTable,
    ItemTable
)
