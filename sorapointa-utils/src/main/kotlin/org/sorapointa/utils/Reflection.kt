package org.sorapointa.utils

import kotlin.reflect.KClass

val KClass<*>.qualifiedOrSimple
    get() = this.qualifiedName ?: this.simpleName
