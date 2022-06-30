package org.sorapointa.utils

import org.jline.style.StyleExpression
import org.sorapointa.console.Console

private val styleExpression = StyleExpression()

/**
 * Color [String] to Ansi
 *
 * Example:
 * ```kotlin
 * println("@{bold,fg:yellow Hello, }@{bold,fg:magenta World!}".color())
 * ```
 * @see [StyleExpression]
 */
fun String.color(): String? = styleExpression.evaluate(this).toAnsi(Console.terminal)
