package org.sorapointa.config

import org.sorapointa.data.provider.DataFilePersist
import org.sorapointa.utils.I18nConfig

internal val registeredConfig: List<DataFilePersist<*>> = listOf(
    I18nConfig,
)
