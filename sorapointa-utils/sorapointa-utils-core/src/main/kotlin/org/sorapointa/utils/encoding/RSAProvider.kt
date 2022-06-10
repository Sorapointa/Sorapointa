package org.sorapointa.utils.encoding

import java.security.*
import java.security.spec.MGF1ParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource

object RSAProvider {
    val base64: Base64Provider = Base64Impl.Default

    private val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")

    private val cipher: Cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")

    private var oaepSpec =
        OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec("SHA-1"), PSource.PSpecified.DEFAULT)

    fun keyPairGenerate(): KeyPair {
        val kpg = KeyPairGenerator.getInstance(keyFactory.algorithm, keyFactory.provider)
        return kpg.genKeyPair()
    }

    private fun getPublicKey(string: String): PublicKey {
        return keyFactory.generatePublic(X509EncodedKeySpec(base64.decode(string)))
    }

    private fun getPrivateKey(string: String): PrivateKey {
        return keyFactory.generatePrivate(PKCS8EncodedKeySpec(base64.decode(string)))
    }

    fun encryptWithPublicKey(publicKey: String, data: String): String {
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(publicKey), oaepSpec)
        val final = cipher.doFinal(data.toByteArray())
        return base64.encode(final)
    }

    fun decryptWithPrivateKey(privateKey: String, data: String): String {
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(privateKey), oaepSpec)
        val final = cipher.doFinal(base64.decode(data))
        return String(final)
    }
}
