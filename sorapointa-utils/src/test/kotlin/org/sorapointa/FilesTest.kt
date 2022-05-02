package org.sorapointa

import org.junit.jupiter.api.Test
import org.sorapointa.utils.globalWorkDirectory
import org.sorapointa.utils.resolveWorkDirectory

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
