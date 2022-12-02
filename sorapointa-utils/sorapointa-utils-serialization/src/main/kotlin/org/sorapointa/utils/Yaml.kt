package org.sorapointa.utils

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration

val lenientYaml = Yaml(configuration = YamlConfiguration(strictMode = false))
