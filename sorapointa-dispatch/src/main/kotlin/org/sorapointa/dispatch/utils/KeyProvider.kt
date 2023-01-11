package org.sorapointa.dispatch.utils

import io.ktor.network.tls.extensions.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sorapointa.dispatch.DispatchConfig
import org.sorapointa.utils.isWindows
import java.io.File
import java.security.KeyStore
import kotlin.text.toCharArray

object KeyProvider {

    const val DEFAULT_CERT_NAME = "sorapointa-cert"
    const val DEFAULT_ALIAS = "sorapointa-dispatch-cert"
    const val DEFAULT_CERT_PASSWORD = "sorapointa-dispatch-private"
    const val DEFAULT_KEY_STORE_PASSWORD = "sorapointa-dispatch"
    private const val DEFAULT_EXPIRED_DAYS = 365 * 10L // 10 Years

    private val defaultKeyStoreType = if (isWindows) "PKCS12" else "JKS"
    val defaultKeyStoreFileExtension = if (isWindows) ".pfx" else ".jks"

    private val ANIME_URL_1 = "Ki5taWhveW8uY29t".decodeBase64String()
    private val ANIME_URL_2 = "Ki55dWFuc2hlbi5jb20=".decodeBase64String()
    private val ANIME_URL_3 = "Ki5ob3lvdmVyc2UuY29t".decodeBase64String()

    /* ktlint-disable max-line-length */
    // These keys may have the DMCA risk, so we encoded them.
    val queryCurrRSAKeySet = hashMapOf(
        4 to KeySet(
            "PFJTQUtleVZhbHVlPjxNb2R1bHVzPmxDd2RZcnZlb3pZWWNLT1N6NGNqQmZPUnZkNlBPWlN4c005SnliV3ZUYjlycjFxR2h1bGdvTmNNQjBzVUE0WG5mTmx0L2FhVCtKS1NURWd5bnlYOG9mNzRObXU3ME1STzJOZW1pMFluSTU2Z0syZjB0SWRtcEZLbm9qZ0RUbExzbFFuS0J6Y0svZWxiY1gyWEUzRk1LL2hBMnJrSkJJTWtJc1hKMjNuZld5LzZLRkIvbmhYZnQrd3pEYWhZbXphb0xLc2dxNHhRSW5CNm4wZFVTa0ZOU01WKzk4Q1JqaCtZN3BYbHlFZ2xEWHhqK0loQlZzbDhzNDFjOXZtZ0xIV1M3ZmVNdWZiZXFrbzgzZkx2MkdsSS9hVTBwdm1ZcjlMeWY0a2dQTXA1YVRxZXlDbS96dGIzYnA1UW9XN1MyaGxHUDZndHhHcjRzL2xNcFpONVlnVFpiUT09PC9Nb2R1bHVzPjxFeHBvbmVudD5BUUFCPC9FeHBvbmVudD48L1JTQUtleVZhbHVlPg==".decodeBase64String(),
            "PFJTQUtleVZhbHVlPjxNb2R1bHVzPnlheHFqUEpQNStJbm5mdjVJZGZRcVkvZnRTKytsbkRSZTNFY3pOa0lqRVNXWGhIU09sakV3OWI5QysvQnRGK2ZPOVFaTDdaNzQyeTA2ZUlkdnNNUFFLZEdmbEIyNis5T1o4QUY0U3BYRG4zYVZXR3I4KzlxcEI3QkVMUlpJL1BoMkZsRkw0Y29iQ3pNSHVubmNXOHpUZk1JZDQ4K2ZnSGtBekNqUmw1ckM2WFQwWWdlNitlS3BYbUYraHIwdkdZV2lUenFQelRBQmw0NFdabzNydzB5dXJaVHprcm1SRTRrUjJWemtqWS9yQm5RQWJGS0tGVUtzVW96akNYdlNhZzRsNDYxd0RraG1teWl2cE5rSzVjQXh1RGJzbUMzOWlxYWdNdDk0MzhmYWpMVnZZT3ZwVnM5Y2k1dGlMY2JCdGZCNFJmL1FWQWtxdFRtODZaME8zZTdEdz09PC9Nb2R1bHVzPjxFeHBvbmVudD5BUUFCPC9FeHBvbmVudD48UD4vYXVGeDg0RDdVbHJmdUZRY3A1dCtuMnNleDdIajZrYkszY3AyN3RaMm82Zml4N0diSm9HNklkQnhSeUU4TldWcit1NUJuYlQ3d3NlRE1FT2pTYnl4anVDbC92WGxSWDAxSlVoRVBUQzdicElwR1NVNFhNbmdjRTdCVDJFRVl0S2RGUW5QSzlXVzNrN3NUMkVDL3JWSUt1OVlFUnlqRFppY28xQXZDK014VWs9PC9QPjxRPnk0YWhKdmNEKzZXcTJuYk9uRlVCeVZoNzl0SWkxbGxNNVJZL3BWdmlFNklmRWduU2ZVZjFxbnFDczVpUW45aWZpQ0RKak1xYitlZ1hYQmMvdEdQL0U1cUdlOHlUT0VaMlk1cHU4VDBzZmtmQkJOYkVFRlpPUm5PQUZ0aTF1RDRua3hOd3FvbHJKeUZKR01tUDdGZjUzM1N1MlZLNzl6YnR5R1ZKRW9BZGRaYz08L1E+PERQPkZUY0lIRHE5bDFYQm1MM3RSWGk4aCt1RXhsTS9xMk1nTTVWbXVjckViQVBya2U0RCtFYzFkck1CTENRRGRrVFduUHpnMzRxR2xRSmdBLzhOWVg2MVpTREsvajBBdmFZMWNLWDhPdmZOYWFaZnR1ZjJqNWhhNEg0eG1uR1hud1FBT1JSa3A2MmVVazRrVU9GdExyZE9wY25YTDdycHZaSTZ6NHZDc3pwaTBvaz08L0RQPjxEUT5wM2xaRWw4Zy8rb0s5VW5lS2ZZcFNpMXRsR1RHRmV2VndvelVRcFdoS3RhMUNucmFvZ3ljc25PdEtXdlpWaTlDMXhsandGN1lpb1BZOVFhTWZUdnJvWTMrSzlEak0rT0hkOTZVZkI0Q2hzYzBwVzYwVjEwdGUvdCs0MDNmK29QcXZMTzZlaG9wK2tFQmpVd1BDa1E2Y1EzcTh4bUpZcHZvZm9ZWjR3ZFpObkU9PC9EUT48SW52ZXJzZVE+Y0J2RmE3KzJmcEYvV2JvZFJiM0VhR09lMjJDMU5IRmx2ZGtnTnpiNHZLV1RpQkdpeDYwTW1hYjcyaXlJbkVkWnZmaXJEZ0pvb3U2N3RNeSt5ckt4bHZ1Wm9vRUxHZzR1SU0yb1NrS1duZjBlekN5b3Z5K2Q2MkpxTkdtU2dFU3gxdk5obTZKa05NOFhVYUtQYjJxbnhqYVY1TWNzcmQ1TnhoZzdwNXE3SkdNPTwvSW52ZXJzZVE+PEQ+c3BtdHR1cjAxdCtTeERlYzExcmdJUG9ZWE1aT203NkgxakZERnlyeGhmOUx4ejB6RjViN2twQTNneld1THdZcjUza2JZUVRUeklHOTZnN2sxc2E2SUVERGppUEdYWVdOd3hYc1h3NzNFQTltcHd5Ymtxa3BvUFRYZCtxdnNzWk44U0tGd2VTSmFOdDNYYjA1eVZ4NGJBVGFMNys4MFN6dGQrSEFCeGFnNkNzN2VSQkI2M3RMSkZISitoNHh6bnBPbk9kNDc2U3ErUzBxNjRzTWVZRExtUCsyVWlGQTZQVmhtTzlLbTBCUm1PbXpwVi9jZkxqWTNCUmZ1MHM3UkZVUHI0U2YvdXhMOEttaWE4ck1IcU5KZmRVeWpQVm1qTHNLTG5Dbm5IbFZyc3B4TU9oaGs4UEZFeTdaYlhwQ3hudW0wdkdNV1BIMWNKeXBFMGNDV01BQ1VRPT08L0Q+PC9SU0FLZXlWYWx1ZT4=".decodeBase64String(),
        ),
        5 to KeySet(
            "PFJTQUtleVZhbHVlPjxNb2R1bHVzPjE1UkJtL3ZBUlkwYXhZa3NJbWhzVGljcHYwOU9ZZlM0K3dDdm1FN3BzT3ZaaFcyVVJaMlJsZjVEc0V0dVJHLzd2NVcvMm9icXFWa2YrMWRvclBjUjJpcXJZWjRWVlBmN0tVM0NncWgwa3pMR3hXT3BHeHp3SlVMRXlGVmFpTURXYms3Z3I4cmlrL2pZeWhMaUxjNTJ6ejNFM3doVFVQbGVLaE9oWG54eDFpT0tZK1RQVkk4akpmRE5pUW9oMFV2Z2pua2lnSi9zYVB6am9nZWlnLzRNY0JjNGw1Y0RrdnR0a0tRS3E3b1hlOU9DQkNsZ0tsWWpjYzFDTmFsd01sVHo3TnZMRWtvK1pMVGdwQStrRWxadW15QlhUNjdtbVc3dDdJRFhvcnNjQUk3YXV3dXNLV21xNzk3YWxGa1EvNnNVcXM4S0tHbnFRMmZ3SGZhL1JZRGhFdz09PC9Nb2R1bHVzPjxFeHBvbmVudD5BUUFCPC9FeHBvbmVudD48L1JTQUtleVZhbHVlPg==".decodeBase64String(),
            "PFJTQUtleVZhbHVlPjxNb2R1bHVzPnNKYkZwM1djc2lvampkUXRWblR1dnRhd0wybTRYeEs5M0Y2bENuRndjWnFVUDM5dHhGR0dscm9nSE1xcmV5YXdJVU43RTVzaHR3R3ppZ3pqVzhMeTVDcnlCSnBYUDNlaE5UcUpTN2VtYis5TGxDMTlPeGExZVF1VVFuYXRnY3NkMTZEUEg3a0o1SnpOM3ZYbmh2VXlrNFFmaWNkbW0wdWs3RlJhTllGaTdFSnM0eHlxRlRycDNyRFowZHpCSHVtbEllSzFvbTdGTnQ2TnlpdmdwK1V5Yk83a2wwTkxGRWVTbFY0Uys3b2ZpdFdRc081eFlxS0F6U3p6K0tJUlFjeEppZEdCbFoxSk4vZzVEUERweC96dHZPV1lVbE03VFlrNnhOM2ZvY1pwVTBrQnpBdy9ybjk0eVc5ejhqcFhmemsrTXZXelZML0hBY1B5NHlTd2theTBOdz09PC9Nb2R1bHVzPjxFeHBvbmVudD5BUUFCPC9FeHBvbmVudD48UD4xOXdRVUlTWHRwbm1DckVaZmJ5WjZJd095OFpDVmFWVXRiVGpWYThVeWZOZ2x6ekpHM3l6Y1hVM1gzNXY1L0hOQ0hhWGJHMnFjYlFMVGhuSEJBK29iVzNSRG8rUTQ5Vjg0WmgxZlVOSDBPTkhIdUMwOWtCLy9nSHF6bi80bkxmMWFKMk8wTnJNeXJaTnNaMFpLVUtRdVZDcVdqQk9tVE5VaXRjYzhScFhaOHM9PC9QPjxRPjBXMDlQT00vSXQ3Um9WR0krY2ZiYmdTUm16Rm85a3pTcDVsUDdpWjgxYm52VU1hYnUybnYzT2VHYzNQbWRoMVpKRlJ3NmlETTZWVmJHMHV6OGcrZjgrSlQzMlhkcU03TUpBbWdmY1lmVFZCTWlWbmgzMzBXTmtlUnJHV3FRekIyZjJXciswdkpqVThDQUFjT1dEaDBvTmd1SjFsMVRTeUt4cWRMOEZzQTM4VT08L1E+PERQPnVkdDFBSjdwc2dPWW1xUVorclVsSDZGWUxBUXNvV21WSWs3NVhwRTlLUlV3bVlkdzhRWFJ5MkxOcHA5SzR6N0M5d0tGSm9yV01zaCs0MlEyZ3p5b0hIQnRqRWY0elBMSWI4WEJnM1VtcEtqTVY3M0traXkvQjRuSERyNEk1WWRPK2lDUEV5MFJINGtRSkZuTGpFY1FMVDlUTGd4aDRHN2Q0QjJQZ2RqWVlUaz08L0RQPjxEUT5yZGdpVjJMRVRDdnVsQnpjdVl1ZnFPbjkvSGU5aTRjbDdwNGpiYXRoUVFGQm1TbmtxR1ErQ24vZWFnUXhzS2FZRXNKTm9PeHRiTnUvN3g2ZVZ6ZUZMYXdZdDM4VnkwVXV6Rk41ZUM1NFdYTm90VE41ZmsyVm5LVTRWWVZuR3JNbUNvYlpocGJZem9aaFFLaWF6YnkvZzYwd1V0Vzl1N3hYenFPZE0vNDI4WWs9PC9EUT48SW52ZXJzZVE+Y0d4RHNkVVc2Qi9CL256OVFnSWhmbktyYXVDYTgvU0VWanpvSEE2YmRsTEpOYXc4SGxxMmNXMDBaY0NHbFhPWExDQkJObDlObjdyZjAwMTY5VEtGeDJ1ck5uRUs1MldLdU9PUFBEYkR1RXdBdHVvYXJQOGZ4MjFUbkY5ZDRFOXVrbUo0QUJ4M29lOFkxaWEveW9DQ01MM0w0TDZGYk9wYnUydkdpMUw2em1vPTwvSW52ZXJzZVE+PEQ+UE1wYWxyQnRWZ1Fkb3ppVXR2dWdLTUE5Zk1UM1BIdDJNc08rS3g4c0oxK2dnMDk1MlNoN25hM0xXajRHMUdsWUhzdGROajJrV0p6VVVzVG5DL0xMclBKL3lFZmRtekt5bzJGWVhHR0hnV2N1Ykg5UWFpUUNLdjVxZG9ybVpoVW5XOUMzSE9PVlhVY0J0Q3lSSEt1U1VxZ2NOMUVXcUlWYzdDS0p2M3VnTTFhRVA1SEYvSWJEQW1mS2RsbEpkMHRzdEtMUDlBZEEydi81UitRcEVGckczUUo5VHVZNHRuR2pMcDgwREVkMEZ3RWs4Y0xLSDVvTzhSdUxIdWRLZHhKVHdtNy9qeGdud091Q1Z0bXhjSmlnRGxUUHcwd081b1F5Q2cxWUlWQldnUnhHUVJTaG9mc0dWWjNkUlFWRStjTm5VSGdHYVN0V2hFVHhybnpjNnBMQnFRPT08L0Q+PC9SU0FLZXlWYWx1ZT4=".decodeBase64String(),
        ),
    )

    // This key pair is from Grasscutter
    val signKeySet = KeySet(
        "<RSAKeyValue><Modulus>xbbx2m1feHyrQ7jP+8mtDF/pyYLrJWKWAdEv3wZrOtjOZzeLGPzsmkcgncgoRhX4dT+1itSMR9j9m0/OwsH2UoF6U32LxCOQWQD1AMgIZjAkJeJvFTrtn8fMQ1701CkbaLTVIjRMlTw8kNXvNA/A9UatoiDmi4TFG6mrxTKZpIcTInvPEpkK2A7Qsp1E4skFK8jmysy7uRhMaYHtPTsBvxP0zn3lhKB3W+HTqpneewXWHjCDfL7Nbby91jbz5EKPZXWLuhXIvR1Cu4tiruorwXJxmXaP1HQZonytECNU/UOzP6GNLdq0eFDE4b04Wjp396551G99YiFP2nqHVJ5OMQ==</Modulus><Exponent>AQAB</Exponent></RSAKeyValue>",
        "<RSAKeyValue><Modulus>xbbx2m1feHyrQ7jP+8mtDF/pyYLrJWKWAdEv3wZrOtjOZzeLGPzsmkcgncgoRhX4dT+1itSMR9j9m0/OwsH2UoF6U32LxCOQWQD1AMgIZjAkJeJvFTrtn8fMQ1701CkbaLTVIjRMlTw8kNXvNA/A9UatoiDmi4TFG6mrxTKZpIcTInvPEpkK2A7Qsp1E4skFK8jmysy7uRhMaYHtPTsBvxP0zn3lhKB3W+HTqpneewXWHjCDfL7Nbby91jbz5EKPZXWLuhXIvR1Cu4tiruorwXJxmXaP1HQZonytECNU/UOzP6GNLdq0eFDE4b04Wjp396551G99YiFP2nqHVJ5OMQ==</Modulus><Exponent>AQAB</Exponent><D>xHmGYY8qvmr1LnkrhYTmiFOP2YZV8nLDqs6cCb8xM+tbQUr62TwOS0m/acwL6YnPu4Qx/eI1/PfvHTXzu6pQA7FTRECQcbr9qNTAo6QkZJgWc+dOiARlOtCrdY+ZMHQhHq4E1tat++c+MJfH+y5ki9lOlrynHaI01caIQZCFCe7IbZprpA4tmJzH3uk/9iblwwy/K7yHJ36+RDAoD0LPsS3ixBqyCXaVMtYiGGWK8766ScH/RCS9w9Hu45KW7wEGfBBfWIRIsyYTpnc06luD4FtslGh2Hd6uUI4iC8uwAvqDmKE2ZZ90X4zzsZfm2I3jDlpapILaT0JABOCOuMPEWQ==</D><P>8tHjikSSZN18ggXxm3MGJV8Nnb1tP3onQJZcZXOnzHptK7knmOWzuw/wMRyMZnq8ewsY6+Rw3HNydHeX/kc7PpMi69W5SbfpvWMeW2rXFlK2MZ4pmzWKGElK7aUgD5OsrwUJGcoBEnS6CFcY1kUi2B4zbfRKCOnZEvghJcnvbhc=</P><Q>0HJLZHA2lRi+QJJkdIWdAz+OrWOV3HD7SniMAalYuKURoD/zFZSdmucKs8UX+32WWlt1NH90Ijye0gwDLZ0fghQfJgpRqHIdLMIBQ0qlLSzjfeSfmHL20a+fuPK44nh2T0WjU8hkzup/OaR0IFtfc0XZManM69tgYkccLeyxWvc=</Q><DP>0ckOik32INjOklNqS0BURgNaczbOZTI3KXD+wNPsXBhFq6nbERkbb/k0LmoYzw0pPDD5Rgxmib/gWcldct29zLE4UYKkA5G2it5QwvCKhYnOSQ35qlPWTGc+KhUonuyaG9gA5dwFkxlwBHajSbQPh6KIEm4lbJAE8IOZt9lAV98=</DP><DQ>qlyvh7A6vBLT87xyA9XsJOp+NvIMWnWwvAXYD8eTrp2i0UFS8FFdmmu4kILGfhH/n2veWADPLugyueN9eXtQdCTz7EhEwxI5DAqns5K/ezOT3qHLWnKjjW8ncKZYOyhPMazttx0yXvbC8p6ZFpT3ZyQwRmnMBPxwQwJxYotvzLM=</DQ><InverseQ>MibG8IyHSo7CJz82+7UHm98jNOlg6s73CEjp0W/+FL45Ka7MF/lpxtR3eSmxltvwvjQoti3V4Qboqtc2IPCt+EtapTM7Wo41wlLCWCNx4u25pZPH/c8g1yQ+OvH+xOYG+SeO98Phw/8d3IRfR83aqisQHv5upo2Rozzo0Kh3OsE=</InverseQ></RSAKeyValue>",
    )
    /* ktlint-enable max-line-length */

    suspend fun getCertsFromConfigOrGenerate(): KeyStore = withContext(Dispatchers.IO) {
        val dispatchConfig = DispatchConfig.data
        val keyStoreFile = File(dispatchConfig.certification.keyStoreFilePath)
        if (!keyStoreFile.exists()) {
            val alias: String = DEFAULT_ALIAS
            val privateKeyPassword: String = DEFAULT_CERT_PASSWORD
            val keyStorePassword: String = DEFAULT_KEY_STORE_PASSWORD
            val expiredDays: Long = DEFAULT_EXPIRED_DAYS
            val generatedKeyStore = buildKeyStore {
                keyStore = defaultKeyStoreType
                certificate(alias) {
                    hash = HashAlgorithm.SHA256
                    sign = SignatureAlgorithm.RSA
                    keySizeInBits = 2048
                    password = privateKeyPassword
                    daysValid = expiredDays
                    hosts = listOf("localhost", ANIME_URL_1, ANIME_URL_2, ANIME_URL_3)
                    keyType = KeyType.Server
                }
            }
            generatedKeyStore.saveToFile(keyStoreFile, keyStorePassword)
            generatedKeyStore.saveCertToFile(
                File(keyStoreFile.parentFile, keyStoreFile.nameWithoutExtension + ".cert"),
                alias,
            )
            dispatchConfig.certification = DispatchConfig.Certification(
                keyStore = defaultKeyStoreType,
                keyAlias = alias,
                keyStorePassword = keyStorePassword,
                privateKeyPassword = privateKeyPassword,
            )
            DispatchConfig.save()
            generatedKeyStore
        } else {
            fromCertFile(keyStoreFile, dispatchConfig.certification.keyStorePassword)
        }
    }

    private fun fromCertFile(certFile: File, password: String): KeyStore {
        return KeyStore.getInstance(DispatchConfig.data.certification.keyStore)?.let {
            it.load(certFile.inputStream(), password.toCharArray())
            it
        } ?: throw IllegalStateException("Failed to load key store")
    }

    data class KeySet(
        val publicKey: String,
        val privateKey: String,
    )
}
