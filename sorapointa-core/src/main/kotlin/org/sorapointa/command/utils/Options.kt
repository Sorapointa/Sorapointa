package org.sorapointa.command.utils

import moe.sdl.yac.parameters.groups.ChoiceGroup
import moe.sdl.yac.parameters.groups.OptionGroup
import moe.sdl.yac.parameters.groups.groupSwitch
import moe.sdl.yac.parameters.options.FlagOption
import moe.sdl.yac.parameters.options.RawOption
import moe.sdl.yac.parameters.options.switch

/**
 * Make a multikey pair to group option
 *
 * ### Example:
 *
 * ```
 * option().switch(
 *   setOf("--foo", "-F") to Foo(),
 *   listOf("-b", "--bar") to Bar(),
 * )
 * ```
 */
fun <T : Any> RawOption.switchSet(vararg choices: Pair<Collection<String>, T>): FlagOption<T?> =
    switch(
        choices.flatMap { (keys, value) ->
            keys.map { it to value }
        }.toMap()
    )

/**
 * Convert the option into a set of flags that each map to an option group.
 * Make a multikey pair to option group
 * ### Example:
 *
 * ```
 * option().groupSwitch(
 *   setOf("--foo", "-F") to FooOptionGroup(),
 *   setOf("--bar", "-b") to BarOptionGroup()
 * )
 * ```
 */
fun <T : OptionGroup> RawOption.groupSwitchSet(vararg choices: Pair<Collection<String>, T>): ChoiceGroup<T, T?> =
    groupSwitch(
        choices.flatMap { (keys, value) ->
            keys.map { it to value }
        }.toMap()
    )
