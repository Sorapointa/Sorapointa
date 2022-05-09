package org.sorapointa.utils.encoding

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class HexTest {
    @Test
    fun hexEncode() =
        assertEquals(
            "f09f9882e39c89e381b2e38289e3818ce381aa6173666a61646c6b2626262128383233",
            "😂㜉ひらがなasfjadlk&&&!(823".toByteArray().hex
        )
}
