package org.sorapointa.server.network

import com.squareup.wire.Message
import com.squareup.wire.ProtoAdapter
import org.sorapointa.event.broadcastEvent
import org.sorapointa.events.HandlePacketEvent
import org.sorapointa.events.HandlePreLoginPacketEvent
import org.sorapointa.game.Player
import org.sorapointa.game.impl
import org.sorapointa.proto.PacketHead
import org.sorapointa.proto.SoraPacket
import org.sorapointa.utils.uncheckedCast

internal interface IncomingPacketHandler<Req, S, C>
    where Req : Message<*, *>, S : NetworkHandlerStateI, C : PacketHandlerContext<S> {

    val cmdId: UShort

    fun parsing(data: ByteArray): Req

    suspend fun C.handle(soraPacket: SoraPacket)
}

internal open class PacketHandlerContext<S : NetworkHandlerStateI>(
    val state: S,
    val metadata: PacketHead,
) {

    fun sendPacket(packet: OutgoingPacket<*>) {
        state.networkHandler.sendPacket(packet, metadata)
    }

    suspend fun sendPacketSync(packet: OutgoingPacket<*>) {
        state.networkHandler.sendPacketSync(packet, metadata)
    }
}

internal open class PlayerPacketHandlerContext(
    val player: Player,
    metadata: PacketHead,
) : PacketHandlerContext<NetworkHandler.Logged>(
    state = player.impl().networkHandler.state.getStateInstance().uncheckedCast(),
    metadata = metadata,
)

internal abstract class AbstractPacketHandler<Req, S, C>(
    override val cmdId: UShort,
) : IncomingPacketHandler<Req, S, C>
    where Req : Message<*, *>, S : NetworkHandlerStateI, C : PacketHandlerContext<S> {

    protected abstract val adapter: ProtoAdapter<Req>

    override fun parsing(data: ByteArray): Req = adapter.decode(data)

    override suspend fun C.handle(soraPacket: SoraPacket) {
        handle(parsing(soraPacket.data))
    }

    abstract suspend fun C.handle(packet: Req)
}

internal abstract class PlayerPacketHandler<Req : Message<*, *>>(
    cmdId: UShort,
) : AbstractPacketHandler<Req, NetworkHandler.Logged, PlayerPacketHandlerContext>(cmdId) {

    override suspend fun PlayerPacketHandlerContext.handle(
        packet: Req,
    ) {
        HandlePacketEvent(player, packet, metadata, adapter).broadcastEvent {
            handlePacket(packet)
        }
    }

    abstract suspend fun PlayerPacketHandlerContext.handlePacket(packet: Req)
}

internal abstract class PreLoginPacketHandler<Req, S>(cmdId: UShort) :
    AbstractPacketHandler<Req, S, PacketHandlerContext<S>>(cmdId)
    where Req : Message<*, *>,
          S : NetworkHandlerStateI {

    protected abstract suspend fun PacketHandlerContext<S>.handlePacket(packet: Req)

    override suspend fun PacketHandlerContext<S>.handle(packet: Req) {
        HandlePreLoginPacketEvent(state.networkHandler, packet, metadata, adapter).broadcastEvent {
            handlePacket(packet)
        }
    }
}

internal object IncomingPacketFactory {

    private val incomingPacketHandlers = listOf<IncomingPacketHandler<*, *, *>>(
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

    suspend inline fun <reified S, reified C> handle(context: C, packet: SoraPacket)
        where S : NetworkHandlerStateI, C : PacketHandlerContext<S> {
        val handler = incomingPacketHandlers
            .firstOrNull { it.cmdId == packet.cmdId }
            ?: return
        val cast = runCatching {
            handler.uncheckedCast<IncomingPacketHandler<*, S, C>>()
        }.getOrElse {
            throw IllegalStateException(
                "Handler ${handler::class.simpleName} can not be cast with " +
                    "state: ${S::class.simpleName}, context: ${C::class.simpleName}",
            )
        }
        cast.run { context.handle(packet) }
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
