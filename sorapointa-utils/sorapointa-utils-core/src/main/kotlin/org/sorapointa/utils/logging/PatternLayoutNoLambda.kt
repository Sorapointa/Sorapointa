package org.sorapointa.utils.logging

import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.pattern.ClassOfCallerConverter
import ch.qos.logback.classic.spi.ILoggingEvent

/**
 * Optimize logger class name for Kotlin.
 * Original [PatternLayoutEncoder] will encode class name with Kotlin lambda.
 * It's redundant and useless.
 *
 * So [PatternLayoutNoLambda] split the name by `$` and remove text after it.
 *
 * Before `SorapointaMain$run$inline$2$1` -> Now `SorapointaMain`
 *
 * @see [ClassOfCallerConverterNoLambda]
 */
class PatternLayoutNoLambda : PatternLayoutEncoder() {
    init {
        val name = ClassOfCallerConverterNoLambda::class.java.name
        PatternLayout.DEFAULT_CONVERTER_MAP["C"] = name
        PatternLayout.DEFAULT_CONVERTER_MAP["class"] = name
        PatternLayout.CONVERTER_CLASS_TO_KEY_MAP[name] = "class"
    }
}

class ClassOfCallerConverterNoLambda : ClassOfCallerConverter() {
    override fun getFullyQualifiedName(event: ILoggingEvent?): String {
        val name = super.getFullyQualifiedName(event)
        return name.split('$').firstOrNull().toString()
    }
}
