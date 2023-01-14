package org.sorapointa.server.network

import com.squareup.wire.Message
import com.squareup.wire.ProtoAdapter
import org.sorapointa.event.broadcastEvent
import org.sorapointa.events.HandleIncomingPacketEvent
import org.sorapointa.events.HandlePreLoginIncomingPacketEvent
import org.sorapointa.game.Player
import org.sorapointa.proto.SoraPacket
import org.sorapointa.utils.uncheckedCast

interface IncomingPacketHandler<Req : Message<*, *>> {

    val cmdId: UShort

    fun parsing(data: ByteArray): Req
}

/**
 * Server passively receives a packet (request),
 * client will require a response, copy metadata in this case
 */
internal abstract class AbstractIncomingPacketHandler<Req : Message<*, *>>(
    override val cmdId: UShort,
) : IncomingPacketHandler<Req> {

    protected abstract val adapter: ProtoAdapter<Req>

    override fun parsing(data: ByteArray): Req = adapter.decode(data)
}

internal abstract class IncomingSessionPacketHandler<Req, S>(
    cmdId: UShort,
) : AbstractIncomingPacketHandler<Req>(cmdId)
    where Req : Message<*, *>, S : NetworkHandlerStateInterface {
    abstract suspend fun S.handle(soraPacket: SoraPacket): OutgoingPacket<*>?
}

internal abstract class IncomingPlayerPacketHandler<Req : Message<*, *>>(
    cmdId: UShort,
) : IncomingSessionPacketHandler<Req, NetworkHandler.PlayerHandlePacketState>(cmdId) {

    override suspend fun NetworkHandler.PlayerHandlePacketState.handle(
        soraPacket: SoraPacket,
    ): OutgoingPacket<*>? = this.bindPlayer.handle(soraPacket)

    abstract suspend fun Player.handle(soraPacket: SoraPacket): OutgoingPacket<*>?
}

internal abstract class IncomingPacketHandlerWithoutResponse<Req>(cmdId: UShort) :
    IncomingPlayerPacketHandler<Req>(cmdId)
    where Req : Message<*, *> {
    protected abstract suspend fun Player.handlePacket(packet: Req)

    override suspend fun Player.handle(soraPacket: SoraPacket): OutgoingPacket<*>? {
        val packet = parsing(soraPacket.data)
        HandleIncomingPacketEvent(this, packet, adapter).broadcastEvent {
            handlePacket(packet)
        }
        return null // No response for this type of packet
    }
}

internal abstract class IncomingPacketHandlerWithResponse<Req, Rsp>(
    cmdId: UShort,
) : IncomingPlayerPacketHandler<Req>(cmdId)
    where Req : Message<*, *>,
          Rsp : OutgoingPacket<*> {

    protected abstract suspend fun Player.handlePacket(packet: Req): Rsp

    override suspend fun Player.handle(soraPacket: SoraPacket): OutgoingPacket<*>? {
        val packet = parsing(soraPacket.data)
        return HandleIncomingPacketEvent(this, packet, adapter).broadcastEvent {
            handlePacket(packet).also {
                it.metadata = soraPacket.metadata
            }
        }
    }
}

internal abstract class IncomingPreLoginPacketHandler<Req, Rsp, S>(cmdId: UShort) :
    IncomingSessionPacketHandler<Req, S>(cmdId)
    where Req : Message<*, *>,
          Rsp : OutgoingPacket<*>,
          S : NetworkHandlerStateInterface {

    protected abstract suspend fun S.handlePacket(packet: Req): Rsp

    override suspend fun S.handle(soraPacket: SoraPacket): OutgoingPacket<*>? {
        val packet = parsing(soraPacket.data)
        return HandlePreLoginIncomingPacketEvent(networkHandler, packet, adapter).broadcastEvent {
            handlePacket(packet).also {
                it.metadata = soraPacket.metadata
            }
        }
    }
}

internal object IncomingPacketFactory {

    private val incomingPacketHandlers = listOf<IncomingSessionPacketHandler<*, *>>(
        GetPlayerTokenReqHandler,
        PingReqHandler,
        PlayerSetPauseReqHandler,
        PlayerLoginReqHandler,
        SetPlayerBornDataReqHandler,
        GetPlayerSocialDetailReqHandler,
        EnterSceneReadyReqHandler,
        SceneInitFinishReqHandler,
        EnterSceneDoneReqHandler,
        PostEnterSceneReqHandler,
        UnionCmdNotifyHandler,
    )

    suspend inline fun <reified S : NetworkHandlerStateInterface> S.tryHandle(
        packet: SoraPacket,
    ): OutgoingPacket<*>? {
        val handler = incomingPacketHandlers
            .firstOrNull { it.cmdId == packet.cmdId }
            ?: return null
        val cast = runCatching {
            handler.uncheckedCast<IncomingSessionPacketHandler<*, S>>()
        }.getOrElse {
            throw IllegalStateException(
                "Handler ${handler::class.simpleName} " +
                    "is not compatible with state ${this::class.simpleName}",
                it,
            )
        }
        return cast.run { handle(packet) }
    }

    /**
     * Only used for debug parsing
     *
     * @param packet packet to parse
     * @return parsed proto
     */
    fun parseToProto(packet: SoraPacket): Message<*, *>? =
        incomingPacketHandlers
            .firstOrNull { it.cmdId == packet.cmdId }
            ?.parsing(packet.data)
}

/*
C: GetPlayerTokenReq
S: GetPlayerTokenRsp

C: PlayerLoginReq

S: OpenStateUpdateNotify
S: StoreWeightLimitNotify
S: PlayerStoreNotify
S: AvatarDataNotify
S: PlayerEnterSceneNotify

S: PlayerLoginRsp

C: GetPlayerSocialDetailReq
S: GetPlayerSocialDetailRsp

C: EnterSceneReadyReq
S: EnterSceneReadyRsp

C: SceneInitFinishReq

S: EnterScenePeerNotify
S: WorldDataNotify
S: WorldPlayerInfoNotify
S: ScenePlayerInfoNotify
S: PlayerEnterSceneInfoNotify
S: PlayerGameTimeNotify
S: SceneTimeNotify
S: SceneDataNotify
S: HostPlayerNotify
S: SceneTeamUpdateNotify

S: SceneInitFinishRsp

C: EnterSceneDoneReq
S: SceneEntityAppearNotify
S: EnterSceneDoneRsp

C: PostEnterSceneReq
S: PostEnterSceneRsp
*/
