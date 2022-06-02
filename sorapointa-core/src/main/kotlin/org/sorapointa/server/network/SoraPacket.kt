package org.sorapointa.server.network

import com.google.protobuf.GeneratedMessageV3
import io.ktor.utils.io.core.*
import org.sorapointa.proto.*
import org.sorapointa.proto.GetPlayerTokenReqOuterClass.GetPlayerTokenReq
import org.sorapointa.proto.PacketHeadOuterClass.PacketHead
import org.sorapointa.proto.PingReqOuterClass.PingReq
import org.sorapointa.proto.QueryCurrRegionHttpRspOuterClass.QueryCurrRegionHttpRsp
import org.sorapointa.proto.RetcodeOuterClass.Retcode
import org.sorapointa.utils.i18n
import org.sorapointa.utils.randomByteArray

abstract class OutgoingPacket(
    val cmdId: UShort,
    var metadata: PacketHead? = null
) {

    abstract fun buildProto(): GeneratedMessageV3
}

internal fun OutgoingPacket.toFinalBytePacket(): ByteArray {
    val packet = this
    return buildPacket {
        writeSoraPacket(packet.cmdId, packet.buildProto(), packet.metadata)
    }.readBytes()
}

internal abstract class GetPlayerTokenRspPacket : OutgoingPacket(
    PacketId.GET_PLAYER_TOKEN_RSP,
) {

    internal class Error(
        private val retcode: Retcode,
        private val msg: String
    ) : GetPlayerTokenRspPacket() {
        override fun buildProto(): GeneratedMessageV3 =
            getPlayerTokenRsp {
                msg = this@Error.msg.i18n()
                retcode = this@Error.retcode.number
            }
    }

    internal class Succ(
        private val tokenReq: GetPlayerTokenReq,
        private val keySeed: ULong,
        private val ip: String
    ) : GetPlayerTokenRspPacket() {
        override fun buildProto(): GeneratedMessageV3 =
            getPlayerTokenRsp {
                uid = tokenReq.accountUid.toInt()
                token = tokenReq.accountToken
                accountType = tokenReq.accountType
                accountUid = tokenReq.accountUid
                isProficientPlayer = true
                secretKeySeed = keySeed.toLong()
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
}

internal abstract class PlayerLoginRspPacket : OutgoingPacket(
    PacketId.PLAYER_LOGIN_RSP
) {

    class Fail(
        private val retcode: Retcode
    ) : PlayerLoginRspPacket() {

        override fun buildProto(): GeneratedMessageV3 =
            playerLoginRsp {
                retcode = this@Fail.retcode.number
            }
    }

    class Succ(
        private val queryCurrentRegionHttpRsp: QueryCurrRegionHttpRsp
    ) : PlayerLoginRspPacket() {
        override fun buildProto(): GeneratedMessageV3 =
            playerLoginRsp {
                val regionInfo = queryCurrentRegionHttpRsp.regionInfo
                isUseAbilityHash = true
                abilityHashCode = 557879627
                clientDataVersion = regionInfo.clientDataVersion
                clientSilenceDataVersion = regionInfo.clientSilenceDataVersion
                gameBiz = "hk4e_global" // TODO: rm hardcode
                clientMd5 = regionInfo.clientDataMd5
                clientSilenceMd5 = regionInfo.clientSilenceDataMd5
                resVersionConfig = regionInfo.resVersionConfig
                clientVersionSuffix = regionInfo.clientVersionSuffix
                clientSilenceVersionSuffix = regionInfo.clientSilenceVersionSuffix
                isScOpen = false
                countryCode = "US"
//            totalTickTime
            }
    }
}

internal class PingRspPacket(
    private val pingReq: PingReq
) : OutgoingPacket(
    PacketId.PING_RSP
) {

    override fun buildProto(): GeneratedMessageV3 =
        pingRsp {
            clientTime = pingReq.clientTime
        }
}
