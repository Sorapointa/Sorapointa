package org.sorapointa.proto

import io.ktor.utils.io.core.*
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals

class ProtoTest {
    @Test
    fun proto() {
        AbilityAppliedAbility(
            ability_name = AbilityString(str = "114514"),
            ability_override = AbilityString(hash = 114514),
            override_map = listOf(
                AbilityScalarValueEntry(
                    float_value = 1.3f,
                    key = AbilityString(str = "1919810"),
                ),
            ),
            instanced_ability_id = 100,
        )
    }

    @Test
    fun `sorapointa packet read write test`() {
        val cmdId = PacketId.GET_PLAYER_TOKEN_REQ
        val randomInt = Random.nextInt()
        val soraPacket = buildPacket {
            writeSoraPacket(
                cmdId,
                GetPlayerTokenReq.ADAPTER,
                GetPlayerTokenReq(uid = randomInt),
                PacketHead(client_sequence_id = randomInt),
            )
        }.readToSoraPacket()

        assertEquals(PacketId.GET_PLAYER_TOKEN_REQ, soraPacket.cmdId)
        assertEquals(randomInt, soraPacket.metadata.client_sequence_id)
        assertEquals(randomInt, GetPlayerTokenReq.ADAPTER.decode(soraPacket.data).uid)
    }
}
