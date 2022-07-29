package org.sorapointa.utils

import moe.sdl.yac.core.jaroWinklerSimilarity

fun suggestTypo(input: String, possibleValues: List<String>) =
    possibleValues.asSequence()
        .filter { input.first() == it.first() }
        .map { it to jaroWinklerSimilarity(input, it) }
        .filter { it.second > 0.8 }
        .sortedByDescending { it.second }
        .map { it.first }
        .firstOrNull()
