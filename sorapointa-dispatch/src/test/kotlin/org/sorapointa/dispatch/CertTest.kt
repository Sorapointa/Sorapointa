package org.sorapointa.dispatch

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.sorapointa.dispatch.utils.KeyProvider

class CertTest {

    @Test
    fun `generate key test`(): Unit = runBlocking {
        KeyProvider.getCertsFromConfigOrGenerate()
    }

}
