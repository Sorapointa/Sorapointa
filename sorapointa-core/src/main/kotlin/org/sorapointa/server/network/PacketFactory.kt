package org.sorapointa.server.network

import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.Parser
import org.sorapointa.game.Player
import org.sorapointa.proto.PacketId
import org.sorapointa.proto.GetPlayerTokenReqOuterClass.GetPlayerTokenReq
import org.sorapointa.proto.SoraPacket


/**
 * Server passively receives a packet (requst),
 * client will require a response, copy metadata in this case
 */
internal abstract class IncomingPacketFactory
<TPacketReq : GeneratedMessageV3>(
    val cmdId: UShort
) {

    protected abstract val parser: Parser<TPacketReq>

    abstract fun Player.handle(soraPacket: SoraPacket): OutgoingPacket?

}

internal abstract class IncomingPacketFactoryWithoutResponse
<TPacketReq : GeneratedMessageV3>(
    cmdId: UShort
): IncomingPacketFactory<TPacketReq>(cmdId) {

    protected abstract fun Player.handlePacket(packet: TPacketReq)

    override fun Player.handle(soraPacket: SoraPacket): OutgoingPacket? {
        handlePacket(parser.parseFrom(soraPacket.data))
        return null // No response for this type of packet
    }


}

internal abstract class IncomingPacketFactoryWithResponse
<TPacketReq : GeneratedMessageV3, TPacketRsp : OutgoingPacket>(
    cmdId: UShort
): IncomingPacketFactory<TPacketReq>(cmdId) {

    protected abstract fun Player.handlePacket(packet: TPacketReq): TPacketRsp

    override fun Player.handle(soraPacket: SoraPacket): OutgoingPacket? {
        val rsp = handlePacket(parser.parseFrom(soraPacket.data))
        rsp.metadata = soraPacket.metadata
        return rsp
    }


}


internal object IncomingPacketFactories {

    private val incomingPacketFactories = listOf<IncomingPacketFactory<*>>(
        GetPlayerTokenReqFactory
    )

    fun Player.handlePacket(packet: SoraPacket): OutgoingPacket? =
        incomingPacketFactories.firstOrNull { it.cmdId == packet.cmdId }
            ?.run {
                handle(packet)
            }

}

internal object GetPlayerTokenReqFactory: IncomingPacketFactoryWithResponse<GetPlayerTokenReq, GetPlayerTokenRspPacket>(
    PacketId.GET_PLAYER_TOKEN_REQ
) {

    override val parser: Parser<GetPlayerTokenReq> = GetPlayerTokenReq.parser()

    override fun Player.handlePacket(packet: GetPlayerTokenReq): GetPlayerTokenRspPacket {

        TODO("Not yet implemented")
    }

}


/*

NetworkHandler (ByteReadPacket) -> IncomingFactories (SoraPacket, find Factory by cmdId) [List Factory]
  -> IncomingFactory (get Serializer of ProtobufData, then parse it)
  -> SomeIncomingPacketFactory (get ProtoBuf only)
  -> may return a (Reponse OutgoingPacket)
  -> NetworkHandler would send it
 */
