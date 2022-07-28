package org.sorapointa.command

import moe.sdl.yac.core.*
import moe.sdl.yac.output.HelpFormatter
import moe.sdl.yac.output.Localization
import moe.sdl.yac.parameters.groups.ChoiceGroup
import moe.sdl.yac.parameters.groups.MutuallyExclusiveOptions
import org.sorapointa.CoreBundle

/** An object to let clikt use i18n */
object CommandLocalization : Localization {
    /** [Abort] was thrown */
    override fun aborted() = CoreBundle.message("clikt.aborted")

    /** Prefix for any [UsageError] */
    override fun usageError(message: String) = CoreBundle.message("clikt.usage.error", message)

    /** Message for [BadParameterValue] */
    override fun badParameter() = CoreBundle.message("clikt.bad.parameter")

    /** Message for [BadParameterValue] */
    override fun badParameterWithMessage(message: String) =
        CoreBundle.message("clikt.bad.parameter.with.message", message)

    /** Message for [BadParameterValue] */
    override fun badParameterWithParam(paramName: String) =
        CoreBundle.message("clikt.bad.parameter.with.param", paramName)

    /** Message for [BadParameterValue] */
    override fun badParameterWithMessageAndParam(paramName: String, message: String) =
        CoreBundle.message("clikt.bad.parameter.with.message.param", paramName, message)

    /** Message for [MissingOption] */
    override fun missingOption(paramName: String) = CoreBundle.message("clikt.missing.option", paramName)

    /** Message for [MissingArgument] */
    override fun missingArgument(paramName: String) = CoreBundle.message("clikt.missing.argument", paramName)

    /** Message for [NoSuchSubcommand] */
    override fun noSuchSubcommand(name: String, possibilities: List<String>): String {
        return CoreBundle.message("clikt.no.such.subcommand") + when (possibilities.size) {
            0 -> ""
            1 -> CoreBundle.message("clikt.no.such.subcommand.one", possibilities.first())
            else -> possibilities.joinToString(
                prefix = CoreBundle.message("clikt.no.such.subcommand.else.prefix"), postfix = ")"
            )
        }
    }

    /** Message for [NoSuchOption] */
    override fun noSuchOption(name: String, possibilities: List<String>): String {
        return CoreBundle.message("clikt.no.such.option") + when (possibilities.size) {
            0 -> ""
            1 -> CoreBundle.message("clikt.no.such.option.one", possibilities.first())
            else -> possibilities.joinToString(
                prefix = CoreBundle.message("clikt.no.such.option.else.prefix"), postfix = ")"
            )
        }
    }

    /**
     * Message for [IncorrectOptionValueCount]
     *
     * @param count non-negative count of required values
     */
    override fun incorrectOptionValueCount(name: String, count: Int): String {
        return when (count) {
            0 -> CoreBundle.message("clikt.incorrect.option.value.count.zero", name)
            1 -> CoreBundle.message("clikt.incorrect.option.value.count.one", name)
            else -> CoreBundle.message("clikt.incorrect.option.value.count.else", name, count)
        }
    }

    /**
     * Message for [IncorrectArgumentValueCount]
     *
     * @param count non-negative count of required values
     */
    override fun incorrectArgumentValueCount(name: String, count: Int): String {
        return when (count) {
            0 -> CoreBundle.message("clikt.incorrect.argument.value.count.zero", name)
            1 -> CoreBundle.message("clikt.incorrect.argument.value.count.one", name)
            else -> CoreBundle.message("clikt.incorrect.argument.value.count.else", name, count)
        }
    }

    /**
     * Message for [MutuallyExclusiveGroupException]
     *
     * @param others non-empty list of other options in the group
     */
    override fun mutexGroupException(name: String, others: List<String>): String {
        return CoreBundle.message(
            "clikt.mutex.group.exception",
            name,
            others.joinToString(
                CoreBundle.message("clikt.mutex.group.exception.separator")
            )
        )
    }

    /** Message for [FileNotFound] */
    override fun fileNotFound(filename: String) = CoreBundle.message("clikt.file.not.found", filename)

    /** Message for [InvalidFileFormat]*/
    override fun invalidFileFormat(filename: String, message: String) =
        CoreBundle.message("clikt.invalid.file.format", filename, message)

    /** Message for [InvalidFileFormat]*/
    override fun invalidFileFormat(filename: String, lineNumber: Int, message: String) =
        CoreBundle.message("clikt.invalid.file.format.with.line.number", filename, lineNumber, message)

    /** Error in message for [InvalidFileFormat] */
    override fun unclosedQuote() = CoreBundle.message("clikt.unclosed.quote")

    /** Error in message for [InvalidFileFormat] */
    override fun fileEndsWithSlash() = CoreBundle.message("clikt.file.ends.with.slash")

    /** One extra argument is present */
    override fun extraArgumentOne(name: String) = CoreBundle.message("clikt.extra.argument.one")

    /** More than one extra argument is present */
    override fun extraArgumentMany(name: String, count: Int) = CoreBundle.message("clikt.extra.argument.many")

    /** Error message when reading flag option from a file */
    override fun invalidFlagValueInFile(name: String) =
        CoreBundle.message("clikt.invalid.flag.value.in.file")

    /** Error message when reading switch option from environment variable */
    override fun switchOptionEnvvar() = CoreBundle.message("clikt.switch.option.envvar")

    /** Required [MutuallyExclusiveOptions] was not provided */
    override fun requiredMutexOption(options: String) = CoreBundle.message("clikt.required.mutex.option", options)

    /**
     * [ChoiceGroup] value was invalid
     *
     * @param choices non-empty list of possible choices
     */
    override fun invalidGroupChoice(value: String, choices: List<String>): String {
        return CoreBundle.message("clikt.invalid.group.choice", value, choices.joinToString())
    }

    /** Invalid value for a parameter of type [Double] or [Float] */
    override fun floatConversionError(value: String) = CoreBundle.message("clikt.conversion.error.float", value)

    /** Invalid value for a parameter of type [Int] or [Long] */
    override fun intConversionError(value: String) = CoreBundle.message("clikt.conversion.error.int", value)

    /** Invalid value for a parameter of type [Boolean] */
    override fun boolConversionError(value: String) = CoreBundle.message("clikt.conversion.error.bool", value)

    /** Invalid value falls outside range */
    override fun rangeExceededMax(value: String, limit: String) =
        CoreBundle.message("clikt.range.exceeded.max", value, limit)

    /** Invalid value falls outside range */
    override fun rangeExceededMin(value: String, limit: String) =
        CoreBundle.message("clikt.range.exceeded.min", value, limit)

    /** Invalid value falls outside range */
    override fun rangeExceededBoth(value: String, min: String, max: String) =
        CoreBundle.message("clikt.range.exceeded.both", value, min, max)

    /**
     * Invalid value for `choice` parameter
     *
     * @param choices non-empty list of possible choices
     */
    override fun invalidChoice(choice: String, choices: List<String>): String =
        CoreBundle.message("clikt.invalid.choice", choice, choices.joinToString())

    /** The `pathType` parameter to [pathDoesNotExist] and other `path*` errors */
    override fun pathTypeFile() = CoreBundle.message("clikt.path.type.file")

    /** The `pathType` parameter to [pathDoesNotExist] and other `path*` errors */
    override fun pathTypeDirectory() = CoreBundle.message("clikt.path.type.directory")

    /** The `pathType` parameter to [pathDoesNotExist] and other `path*` errors */
    override fun pathTypeOther() = CoreBundle.message("clikt.path.type.other")

    /** Invalid path type */
    override fun pathDoesNotExist(pathType: String, path: String) =
        CoreBundle.message("clikt.path.does.not.exist", pathType, path)

    /** Invalid path type */
    override fun pathIsFile(pathType: String, path: String) = CoreBundle.message("clikt.path.is.file", pathType, path)

    /** Invalid path type */
    override fun pathIsDirectory(pathType: String, path: String) =
        CoreBundle.message("clikt.path.is.directory", pathType, path)

    /** Invalid path type */
    override fun pathIsNotWritable(pathType: String, path: String) =
        CoreBundle.message("clikt.path.is.not.writable", pathType, path)

    /** Invalid path type */
    override fun pathIsNotReadable(pathType: String, path: String) =
        CoreBundle.message("clikt.path.is.not.readable", pathType, path)

    /** Invalid path type */
    override fun pathIsSymlink(pathType: String, path: String) =
        CoreBundle.message("clikt.path.is.symlink", pathType, path)

    /** Metavar used for options with unspecified value type */
    override fun defaultMetavar() = CoreBundle.message("clikt.meta.var.default")

    /** Metavar used for options that take [String] values */
    override fun stringMetavar() = CoreBundle.message("clikt.meta.var.string")

    /** Metavar used for options that take [Float] or [Double] values */
    override fun floatMetavar() = CoreBundle.message("clikt.meta.var.float")

    /** Metavar used for options that take [Int] or [Long] values */
    override fun intMetavar() = CoreBundle.message("clikt.meta.var.int")

    /** Metavar used for options that take `File` or `Path` values */
    override fun pathMetavar() = CoreBundle.message("clikt.meta.var.path")

    /** Metavar used for options that take `InputStream` or `OutputStream` values */
    override fun fileMetavar() = CoreBundle.message("clikt.meta.var.file")

    /** The title for the usage section of help output */
    override fun usageTitle(): String = CoreBundle.message("clikt.title.usage")

    /** The title for the options' section of help output */
    override fun optionsTitle(): String = CoreBundle.message("clikt.title.options")

    /** The title for the arguments' section of help output */
    override fun argumentsTitle(): String = CoreBundle.message("clikt.title.arguments")

    /** The title for the subcommands section of help output */
    override fun commandsTitle(): String = CoreBundle.message("clikt.title.commands")

    /** The that indicates where options may be present in the usage help output */
    override fun optionsMetavar(): String = CoreBundle.message("clikt.meta.var.options")

    /** The that indicates where subcommands may be present in the usage help output */
    override fun commandMetavar(): String = CoreBundle.message("clikt.meta.var.command")

    /** Text rendered for parameters tagged with [HelpFormatter.Tags.DEFAULT] */
    override fun helpTagDefault(): String = CoreBundle.message("clikt.help.tag.default")

    /** Text rendered for parameters tagged with [HelpFormatter.Tags.REQUIRED] */
    override fun helpTagRequired(): String = CoreBundle.message("clikt.help.tag.required")

    /** The default message for the `--help` option. */
    override fun helpOptionMessage(): String = CoreBundle.message("clikt.help.option.message")
}
