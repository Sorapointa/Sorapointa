package org.sorapointa.utils

import org.junit.jupiter.api.Test

class FilesTest {
    @Test
    fun resolveGlobalWorkdir() {
        assert(globalWorkDirectory.also(::println).isDirectory)
    }

    @Test
    fun resolveSubWorkDir() {
        assert(resolveWorkDirectory("./config/newfile.txt").also(::println).name == "newfile.txt")
    }
}
