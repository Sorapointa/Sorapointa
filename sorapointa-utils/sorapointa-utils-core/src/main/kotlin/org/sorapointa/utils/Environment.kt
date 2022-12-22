package org.sorapointa.utils

val osName: String by lazy {
    System.getProperty("os.name")
}

val isWindows: Boolean by lazy {
    osName.contains("win", true)
}

val isMac: Boolean by lazy {
    osName.contains("dar", true)
}

val isLinux: Boolean by lazy {
    osName.contains("nix", true) ||
        osName.contains("nux", true) ||
        osName.contains("aix", true)
}
