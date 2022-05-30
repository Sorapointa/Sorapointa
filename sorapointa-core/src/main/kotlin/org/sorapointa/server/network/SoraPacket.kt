package org.sorapointa.server.network

import com.google.protobuf.GeneratedMessageV3
import io.ktor.utils.io.core.*
import org.sorapointa.proto.GetPlayerTokenReqOuterClass.GetPlayerTokenReq
import org.sorapointa.proto.PacketHeadOuterClass.PacketHead
import org.sorapointa.proto.PacketId
import org.sorapointa.proto.getPlayerTokenRsp
import org.sorapointa.proto.toByteString
import org.sorapointa.proto.writeSoraPacket
import org.sorapointa.utils.SorapointaInternal
import org.sorapointa.utils.randomByteArray


internal abstract class OutgoingPacket(
    val cmdId: UShort,
    var metadata: PacketHead? = null
) {

    abstract fun buildProto(): GeneratedMessageV3

}

@OptIn(SorapointaInternal::class)
internal fun OutgoingPacket.toFinalBytePacket(): ByteArray {
    val packet = this
    return buildPacket {
        writeSoraPacket(packet.cmdId, packet.buildProto(), packet.metadata)
    }.readBytes()
}


internal class GetPlayerTokenRspPacket(
    private val tokenReq: GetPlayerTokenReq,
    private val keySeed: Long,
    private val ip: String
) : OutgoingPacket(
    PacketId.GET_PLAYER_TOKEN_RSP,
) {

    override fun buildProto(): GeneratedMessageV3 =
        getPlayerTokenRsp {
            uid = tokenReq.uid
            token = tokenReq.accountToken
            accountType = tokenReq.accountType
            accountUid = tokenReq.accountUid
            isProficientPlayer = true
            secretKeySeed = keySeed
            securityCmdBuffer = randomByteArray(32).toByteString()
            platformType = tokenReq.platformType
            channelId = tokenReq.channelId
            subChannelId = tokenReq.subChannelId
            countryCode = "US"
            clientVersionRandomKey = "aeb-bc90f1631c05"
            regPlatform = 1
            clientIpStr = ip
        }

}


