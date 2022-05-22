package org.sorapointa.command

import moe.sdl.yac.core.*
import moe.sdl.yac.output.HelpFormatter
import moe.sdl.yac.output.Localization
import moe.sdl.yac.parameters.groups.ChoiceGroup
import moe.sdl.yac.parameters.groups.MutuallyExclusiveOptions
import org.sorapointa.utils.i18n

/** An object to let clikt use i18n */
object CommandLocalization : Localization {
    /** [Abort] was thrown */
    override fun aborted() = "clikt.aborted".i18n()

    /** Prefix for any [UsageError] */
    override fun usageError(message: String) = "clikt.usage.error".i18n(message)

    /** Message for [BadParameterValue] */
    override fun badParameter() = "clikt.bad.parameter".i18n()

    /** Message for [BadParameterValue] */
    override fun badParameterWithMessage(message: String) = "clikt.bad.parameter.with.message".i18n(message)

    /** Message for [BadParameterValue] */
    override fun badParameterWithParam(paramName: String) = "clikt.bad.parameter.with.param".i18n(paramName)

    /** Message for [BadParameterValue] */
    override fun badParameterWithMessageAndParam(paramName: String, message: String) =
        "clikt.bad.parameter.with.message.param".i18n(paramName, message)

    /** Message for [MissingOption] */
    override fun missingOption(paramName: String) = "clikt.missing.option".i18n(paramName)

    /** Message for [MissingArgument] */
    override fun missingArgument(paramName: String) = "clikt.missing.argument".i18n(paramName)

    /** Message for [NoSuchSubcommand] */
    override fun noSuchSubcommand(name: String, possibilities: List<String>): String {
        return "clikt.no.such.subcommand" + when (possibilities.size) {
            0 -> ""
            1 -> "clikt.no.such.subcommand.one".i18n(possibilities[0])
            else -> possibilities.joinToString(
                prefix = "clikt.no.such.subcommand.else.prefix".i18n(), postfix = ")"
            )
        }
    }

    /** Message for [NoSuchOption] */
    override fun noSuchOption(name: String, possibilities: List<String>): String {
        return "clikt.no.such.option" + when (possibilities.size) {
            0 -> ""
            1 -> "clikt.no.such.option.one".i18n(possibilities[0])
            else -> possibilities.joinToString(
                prefix = "clikt.no.such.option.else.prefix".i18n(), postfix = ")"
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
            0 -> "clikt.incorrect.option.value.count.zero".i18n(name)
            1 -> "clikt.incorrect.option.value.count.one".i18n(name)
            else -> "clikt.incorrect.option.value.count.else".i18n(name, count)
        }
    }

    /**
     * Message for [IncorrectArgumentValueCount]
     *
     * @param count non-negative count of required values
     */
    override fun incorrectArgumentValueCount(name: String, count: Int): String {
        return when (count) {
            0 -> "clikt.incorrect.argument.value.count.zero".i18n(name)
            1 -> "clikt.incorrect.argument.value.count.one".i18n(name)
            else -> "clikt.incorrect.argument.value.count.else".i18n(name, count)
        }
    }

    /**
     * Message for [MutuallyExclusiveGroupException]
     *
     * @param others non-empty list of other options in the group
     */
    override fun mutexGroupException(name: String, others: List<String>): String {
        return "clikt.mutex.group.exception".i18n(
            name,
            others.joinToString(
                "clikt.mutex.group.exception.separator".i18n()
            )
        )
    }

    /** Message for [FileNotFound] */
    override fun fileNotFound(filename: String) = "clikt.file.not.found".i18n(filename)

    /** Message for [InvalidFileFormat]*/
    override fun invalidFileFormat(filename: String, message: String) =
        "clikt.invalid.file.format".i18n(filename, message)

    /** Message for [InvalidFileFormat]*/
    override fun invalidFileFormat(filename: String, lineNumber: Int, message: String) =
        "clikt.invalid.file.format.with.line.number".i18n(filename, lineNumber, message)

    /** Error in message for [InvalidFileFormat] */
    override fun unclosedQuote() = "clikt.unclosed.quote".i18n()

    /** Error in message for [InvalidFileFormat] */
    override fun fileEndsWithSlash() = "clikt.file.ends.with.slash".i18n()

    /** One extra argument is present */
    override fun extraArgumentOne(name: String) = "clikt.extra.argument.one".i18n(name)

    /** More than one extra argument is present */
    override fun extraArgumentMany(name: String, count: Int) = "clikt.extra.argument.many".i18n(name)

    /** Error message when reading flag option from a file */
    override fun invalidFlagValueInFile(name: String) = "clikt.invalid.flag.value.in.file".i18n(name)

    /** Error message when reading switch option from environment variable */
    override fun switchOptionEnvvar() = "clikt.switch.option.envvar".i18n()

    /** Required [MutuallyExclusiveOptions] was not provided */
    override fun requiredMutexOption(options: String) = "clikt.required.mutex.option".i18n(options)

    /**
     * [ChoiceGroup] value was invalid
     *
     * @param choices non-empty list of possible choices
     */
    override fun invalidGroupChoice(value: String, choices: List<String>): String {
        return "clikt.invalid.group.choice".i18n(value, choices.joinToString())
    }

    /** Invalid value for a parameter of type [Double] or [Float] */
    override fun floatConversionError(value: String) = "clikt.conversion.error.float".i18n(value)

    /** Invalid value for a parameter of type [Int] or [Long] */
    override fun intConversionError(value: String) = "clikt.conversion.error.int".i18n(value)

    /** Invalid value for a parameter of type [Boolean] */
    override fun boolConversionError(value: String) = "clikt.conversion.error.bool".i18n(value)

    /** Invalid value falls outside range */
    override fun rangeExceededMax(value: String, limit: String) =
        "clikt.range.exceeded.max".i18n(value, limit)

    /** Invalid value falls outside range */
    override fun rangeExceededMin(value: String, limit: String) =
        "clikt.range.exceeded.min".i18n(value, limit)

    /** Invalid value falls outside range */
    override fun rangeExceededBoth(value: String, min: String, max: String) =
        "clikt.range.exceeded.min".i18n(value, min, max)

    /**
     * Invalid value for `choice` parameter
     *
     * @param choices non-empty list of possible choices
     */
    override fun invalidChoice(choice: String, choices: List<String>): String {
        return "clikt.invalid.choice".i18n(choice, choices.joinToString())
    }

    /** The `pathType` parameter to [pathDoesNotExist] and other `path*` errors */
    override fun pathTypeFile() = "clikt.path.type.file".i18n()

    /** The `pathType` parameter to [pathDoesNotExist] and other `path*` errors */
    override fun pathTypeDirectory() = "clikt.path.type.directory".i18n()

    /** The `pathType` parameter to [pathDoesNotExist] and other `path*` errors */
    override fun pathTypeOther() = "clikt.path.type.other".i18n()

    /** Invalid path type */
    override fun pathDoesNotExist(pathType: String, path: String) = "clikt.path.does.not.exist".i18n(pathType, path)

    /** Invalid path type */
    override fun pathIsFile(pathType: String, path: String) = "clikt.path.is.file".i18n(pathType, path)

    /** Invalid path type */
    override fun pathIsDirectory(pathType: String, path: String) = "clikt.path.is.directory".i18n(pathType, path)

    /** Invalid path type */
    override fun pathIsNotWritable(pathType: String, path: String) = "clikt.path.is.not.writable".i18n(pathType, path)

    /** Invalid path type */
    override fun pathIsNotReadable(pathType: String, path: String) = "clikt.path.is.not.readable".i18n(pathType, path)

    /** Invalid path type */
    override fun pathIsSymlink(pathType: String, path: String) = "clikt.path.is.symlink".i18n(pathType, path)

    /** Metavar used for options with unspecified value type */
    override fun defaultMetavar() = "clikt.metavar.default".i18n()

    /** Metavar used for options that take [String] values */
    override fun stringMetavar() = "clikt.metavar.string".i18n()

    /** Metavar used for options that take [Float] or [Double] values */
    override fun floatMetavar() = "clikt.metavar.float".i18n()

    /** Metavar used for options that take [Int] or [Long] values */
    override fun intMetavar() = "clikt.metavar.int".i18n()

    /** Metavar used for options that take `File` or `Path` values */
    override fun pathMetavar() = "clikt.metavar.path".i18n()

    /** Metavar used for options that take `InputStream` or `OutputStream` values */
    override fun fileMetavar() = "clikt.metavar.file".i18n()

    /** The title for the usage section of help output */
    override fun usageTitle(): String = "clikt.title.usage".i18n()

    /** The title for the options' section of help output */
    override fun optionsTitle(): String = "clikt.title.options".i18n()

    /** The title for the arguments' section of help output */
    override fun argumentsTitle(): String = "clikt.title.arguments".i18n()

    /** The title for the subcommands section of help output */
    override fun commandsTitle(): String = "clikt.title.commands".i18n()

    /** The that indicates where options may be present in the usage help output */
    override fun optionsMetavar(): String = "clikt.metavar.options".i18n()

    /** The that indicates where subcommands may be present in the usage help output */
    override fun commandMetavar(): String = "clikt.metavar.command".i18n()

    /** Text rendered for parameters tagged with [HelpFormatter.Tags.DEFAULT] */
    override fun helpTagDefault(): String = "clikt.help.tag.default".i18n()

    /** Text rendered for parameters tagged with [HelpFormatter.Tags.REQUIRED] */
    override fun helpTagRequired(): String = "clikt.help.tag.required".i18n()

    /** The default message for the `--help` option. */
    override fun helpOptionMessage(): String = "clikt.help.option.message".i18n()
}
