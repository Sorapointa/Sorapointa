package org.sorapointa.utils

val isWindows: Boolean by lazy {
    osType == OsType.WINDOWS
}

val isMac: Boolean by lazy {
    osType == OsType.MAC
}

val isLinux: Boolean by lazy {
    osType == OsType.LINUX
}

val osType: OsType by lazy {
    System.getProperty("os.name")?.lowercase()?.run {
        when {
            contains("win") -> OsType.WINDOWS
            listOf("nix", "nux", "aix").any { contains(it) } -> OsType.LINUX
            contains("mac") -> OsType.MAC
            contains("sunos") -> OsType.SOLARIS
            else -> OsType.OTHER
        }
    } ?: OsType.OTHER
}

enum class OsType { WINDOWS, LINUX, MAC, SOLARIS, OTHER }
