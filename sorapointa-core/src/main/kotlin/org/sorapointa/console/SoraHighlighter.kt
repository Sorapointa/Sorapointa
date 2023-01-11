package org.sorapointa.console

import org.jline.reader.Highlighter
import org.jline.reader.LineReader
import org.jline.utils.AttributedString
import org.jline.utils.AttributedStringBuilder
import org.jline.utils.AttributedStyle
import org.jline.utils.AttributedStyle.*
import org.sorapointa.command.CommandManager
import java.util.regex.Pattern

object SoraHighlighter : Highlighter {
    override fun highlight(reader: LineReader?, buffer: String?): AttributedString = runCatching {
        if (buffer.isNullOrEmpty()) return AttributedString("")
        if (buffer.isBlank()) return AttributedString(buffer)

        val builder = AttributedStringBuilder()
        val slices = buffer.splitsNoStrip().map { buffer.slice(it) }
        var hasFirstNonBlank = false
        slices.forEach {
            val isFirstNonBlank = it.isNotBlank() && !hasFirstNonBlank
            if (it.isNotBlank()) hasFirstNonBlank = true

            val style: AttributedStyle = when {
                isFirstNonBlank -> if (CommandManager.hasCommand(it)) {
                    DEFAULT.foreground(YELLOW)
                } else {
                    BOLD.foreground(RED)
                }
                it.startsWith("\"") -> DEFAULT.foreground(YELLOW)
                it.startsWith("-") -> DEFAULT.foreground(CYAN)
                else -> DEFAULT
            }

            builder.append(it, style)
        }
        return builder.toAttributedString()
    }.getOrElse { AttributedString(buffer ?: "") }

    override fun setErrorPattern(errorPattern: Pattern?) =
        throw UnsupportedOperationException("setErrorPattern is not unsupported")

    override fun setErrorIndex(errorIndex: Int) =
        throw UnsupportedOperationException("setErrorIndex is not unsupported")
}

private const val QUOTE = '"'
private const val SPLITTER = ' '
private const val ESCAPE = '\\'

private fun String.splitsNoStrip(): List<IntRange> {
    if (this.isBlank()) return listOf(indices)

    val slices = mutableListOf<IntRange>()

    var idx = 0
    var groupStart = 0
    var inQuote = false
    var inSplitter = false
    var inNormal = false

    while (idx < length) {
        val escaped = this.getOrNull(idx - 1) == ESCAPE
        val char = this[idx]
        val isLast = idx == lastIndex
        if (inNormal) {
            when {
                char == QUOTE || char == SPLITTER -> {
                    slices += groupStart until idx
                    inNormal = false
                }
                isLast -> {
                    slices += groupStart..idx
                    inNormal = false
                }
            }
        }
        if (char == QUOTE && !inQuote && isLast) {
            slices += idx..idx
        }
        if (inQuote && isLast) {
            slices += groupStart..idx
            inQuote = false
        }
        if (!escaped) {
            when (char) {
                QUOTE -> {
                    if (inQuote) {
                        inQuote = false
                        slices += groupStart..idx
                    } else {
                        groupStart = idx
                        inQuote = true
                    }
                }
                SPLITTER -> {
                    if (!inQuote) {
                        val hasNext = this.getOrNull(idx + 1) == SPLITTER
                        when {
                            // single space
                            !inSplitter && !hasNext -> {
                                slices += idx..idx
                            }
                            !inSplitter /* && hasNext */ -> {
                                groupStart = idx
                                inSplitter = true
                            }
                            inSplitter && !hasNext -> {
                                inSplitter = false
                                slices += groupStart..idx
                            }
                            /* inSplitter && hasNext -> just ignore*/
                        }
                    }
                }
            }
        }
        if (char != QUOTE && char != SPLITTER) {
            when {
                !inQuote && !inNormal && this.getOrNull(idx - 1) == SPLITTER && isLast -> {
                    slices += idx..idx
                }
                !inQuote && !inNormal -> {
                    inNormal = true
                    groupStart = idx
                }
            }
        }
        idx++
    }

    return slices
}
