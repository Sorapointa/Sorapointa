package org.sorapointa.utils.crypto

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.nio.charset.Charset
import kotlin.test.assertEquals

class RSAKeyTest {

    @Test
    fun `test rsa key`(): Unit = runBlocking {
        /* ktlint-disable max-line-length */
        val key = "<RSAKeyValue><Modulus>z/fyfozlDIDWG9e3Lb29+7j3c66wvUJBaBWP10rB9HTE6prjfcGMqC9imr6zAdD9q+Gr1j7egvqgi3Da+VBAMFH92/5wD5PsD7dX8Z2f4o65Vk2nVOY8Dl75Z/uRhg0Euwnfrved69z9LG6utmlyv6YUPAflXh/JFw7Dq6c4EGeR+KejFTwmVhEdzPGHjXhFmsVt9HdXRYSf4NxHPzOwj8tiSaOQA0jC4E4mM7rvGSH5GX6hma+7pJnl/5+rEVM0mSQvm0m1XefmuFy040bEZ/6O7ZenOGBsvvwuG3TT4FNDNzW8Dw9ExH1l6NoRGaVkDdtrl/nFu5+a09Pm/E0Elw==</Modulus><Exponent>AQAB</Exponent><P>9hdURxe6DnOqSpe6nh2nVLTmxrPXNY+FSFCb4KtuGB5OqmOeAkkQHv2oysabKSLQ/9wa1tNysd/z6LuAOUgZbQ4xvj+Ofh/kAJUPTSLK+QdIY+fQCKYyg04xuQai3tKRKedzDFd1rDAPJO7Z2h9e4Gvvb4ZiqBEAbnYi4DQLSlE=</P><Q>2Fen9TJb+G0Hbt+spyH+tMpAqbXaQFXbQCSRFRBSJuKJDJa55Yqz7ltVpblHmgMiFbGp+0m2cQVZS9ZpMekewH9umNLcInpaSeo1ulrdAhJylXW7DxX4S3P8rb9+2PJnMWiepz4m53nfrjEV0iU6xGP2BmcrzdZy6LoQXEB6vmc=</Q><DP>nNPPNKMtQep6OqEpH3ycV4IVk8mmO47kDGq6e9okBiDCVxm255PyPx2+BMO+u99hO7zkKcWE0VB8WvOqylZlRbeHAcv1HfFq1ugnYSvsF/mJK4nebLSlekJJs7VD9CZStla2XcYayomyDQJeOQBG8VQ3uWX1109GbB7DKQhhrZE=</DP><DQ>cmKuWFNfE1O6WWIELH4p6LcDR3fyRI/gk+KBnyx48zxVkAVllrsmdYFvIGd9Ny4u6F9+a3HG960HULS1/ACxFMCL3lumrsgYUvp1m+mM7xqH4QRVeh14oZRa5hbY36YS76nMMMsI0Ny8aqJjUjADCXF81FfabkPTj79JBS3GeEM=</DQ><InverseQ>F5hSE9O/UKJB4ya1s/1GqBFG6zZFwf/LyogOGRkszLZd41D0HV61X3tKH3ioqgkLOH+EtHWBIwr+/ziIo1eS9uJo/2dUOKvvkuTpLCizzwHd4F+AGG0XID0uK1CpdaA5P3mDdAjWAvw2FfbAL+uZV/G9+R2Ib1yElWLcMELv/mI=</InverseQ><D>rR9ewnJPiiUGF49vcahuKspDVA2sGyC4igjJARO+ed1qv1HI5rrkeG1ZzC/LnEt5oEfwYB1d5fL1Cp8b6kcf6BmZFjWs24rsC/k4QG5S1qqxJmLmVQqEHAJ75E/LSKg1s+34QxLmZ55DM2XAEyGc4GVEmuSHz97t6z/jK1W8mgncyRHiNGK79V0/jOXXZCkK2IKguZEYmIvy4zXCyYaklbfKd+wnScdTxhxYyim+DGaQDZTUYHk7VqRlX0tDyS82oiNTcj0ib+8VmYFYWyvfsEMakhuipmeL6RL0SNcyoqL+QbABTfhn7g+ZqZ9V6PQqc034/7Dtd1aRx/jLfNPsgQ==</D></RSAKeyValue>".parseToRSAKey()
        /* ktlint-enable max-line-length */
        val testStr = "test rsa key"
        with(key!!) { // Should not be null
            assertEquals(testStr, testStr.toByteArray().encrypt().decrypt().toString(Charset.defaultCharset()))
        }
    }
}
