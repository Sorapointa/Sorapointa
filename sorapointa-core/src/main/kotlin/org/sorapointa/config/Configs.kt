@file:OptIn(SorapointaInternal::class)

package org.sorapointa.config

import org.jetbrains.exposed.sql.Table
import org.sorapointa.SorapointaConfig
import org.sorapointa.data.provider.DataFilePersist
import org.sorapointa.dispatch.DispatchConfig
import org.sorapointa.dispatch.data.AccountTable
import org.sorapointa.event.EventManagerConfig
import org.sorapointa.task.CronTasks
import org.sorapointa.utils.I18nConfig
import org.sorapointa.utils.SorapointaInternal

internal val registeredConfig: List<DataFilePersist<*>> = listOf(
    SorapointaConfig,
    I18nConfig,
    EventManagerConfig,
    DispatchConfig,
)

internal val registeredDatabaseTable: List<Table> = listOf(
    CronTasks,
    AccountTable
)
