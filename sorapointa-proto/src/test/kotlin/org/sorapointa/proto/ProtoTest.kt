package org.sorapointa.proto

import io.ktor.utils.io.core.*
import org.junit.jupiter.api.Test
import org.sorapointa.proto.GetPlayerTokenReqOuterClass.GetPlayerTokenReq
import kotlin.random.Random
import kotlin.test.assertEquals

class ProtoTest {
    @Test
    fun proto() {
        abilityAppliedAbility {
            abilityName = abilityString {
                str = "114514"
            }
            abilityOverride = abilityString {
                hash = 114514
            }
            overrideMap.apply {
                add(
                    abilityScalarValueEntry {
                        floatValue = 1.3f
                        key = abilityString {
                            str = "1919810"
                        }
                    }
                )
            }
            instancedAbilityId = 100
        }.toByteString()
    }

    @Test
    fun findName() {
        assertEquals("ABILITY_CHANGE_NOTIFY", findCommonNameFromCmdId(1155u))
    }

    @Test
    fun `sorapointa packet read write test`() {
        val cmdId = PacketId.GET_PLAYER_TOKEN_REQ
        val randomInt = Random.nextInt()
        val soraPacket = buildPacket {
            writeSoraPacket(
                cmdId,
                getPlayerTokenReq { uid = randomInt },
                packetHead { clientSequenceId = randomInt }
            )
        }.readToSoraPacket()

        assertEquals(PacketId.GET_PLAYER_TOKEN_REQ, soraPacket.cmdId)
        assertEquals(randomInt, soraPacket.metadata.clientSequenceId)
        assertEquals(randomInt, GetPlayerTokenReq.parseFrom(soraPacket.data).uid)
    }
}
