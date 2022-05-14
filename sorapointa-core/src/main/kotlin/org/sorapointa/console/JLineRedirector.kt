package org.sorapointa.console

import java.io.PrintStream
import java.util.*

/**
 * Work around, not a good implementation
 */
internal object JLineRedirector : PrintStream(nullOutputStream()) {
    private fun println0() = Console.println()

    private fun println0(x: Any?) = Console.println(x)

    override fun println(x: Any?) = println0(x)

    override fun println(x: Boolean) = println0(x)

    override fun println(x: Char) = println0(x)

    override fun println(x: CharArray) = println0(x.contentToString())

    override fun println(x: Double) = println0(x)

    override fun println(x: Float) = println0(x)

    override fun println(x: Int) = println0(x)

    override fun println(x: Long) = println0(x)

    override fun println(x: String?) = println0(x)

    override fun printf(format: String, vararg args: Any?): PrintStream {
        println0(String.format(format, args = args))
        return this
    }

    override fun format(format: String, vararg args: Any?): PrintStream {
        String.format(format, args = args)
        return this
    }

    override fun format(l: Locale?, format: String, vararg args: Any?): PrintStream {
        println0(String.format(l, format, args = args))
        return this
    }

    override fun printf(l: Locale?, format: String, vararg args: Any?): PrintStream {
        return super.printf(l, format, *args)
    }

    override fun println() = println0()

    override fun append(csq: CharSequence?): PrintStream {
        println0(csq)
        return this
    }

    override fun append(csq: CharSequence?, start: Int, end: Int): PrintStream {
        println0(csq)
        return this
    }

    override fun writeBytes(buf: ByteArray?) = println0(
        if (buf != null) {
            println0(String(buf, Charsets.UTF_8))
        } else "null"
    )

    override fun write(buf: ByteArray) {
        println0(String(buf, Charsets.UTF_8))
    }

    override fun write(buf: ByteArray, off: Int, len: Int) {
        println0(String(buf, Charsets.UTF_8))
    }

    // Below functions are single element print,
    // JLine do not support print above of them, so direct print

    override fun print(x: Any?) = kotlin.io.print(x)

    override fun print(x: Boolean) = kotlin.io.print(x)

    override fun print(x: Char) = kotlin.io.print(x)

    override fun print(x: CharArray) = kotlin.io.print(x.joinToString())

    override fun print(x: Double) = kotlin.io.print(x)

    override fun print(x: Float) = kotlin.io.print(x)

    override fun print(x: Int) = kotlin.io.print(x)

    override fun print(x: Long) = kotlin.io.print(x)

    override fun print(x: String?) = kotlin.io.print(x)

    override fun append(c: Char): PrintStream {
        kotlin.io.print(c)
        return this
    }
}
