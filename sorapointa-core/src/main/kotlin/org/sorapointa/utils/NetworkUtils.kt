package org.sorapointa.utils

import io.ktor.utils.io.core.*
import io.ktor.utils.io.streams.*
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelOutboundInvoker
import kotlinx.coroutines.suspendCancellableCoroutine
import org.sorapointa.proto.START_MAGIC
import org.sorapointa.proto.SoraPacket
import org.sorapointa.proto.packetHead
import org.sorapointa.proto.readToSoraPacket
import java.net.InetSocketAddress

val Channel.host: String
    get() = (remoteAddress() as InetSocketAddress).address.hostAddress
internal suspend fun ChannelFuture.awaitKt(): ChannelFuture {
    suspendCancellableCoroutine<Unit> { cont ->
        cont.invokeOnCancellation {
            channel().close()
        }
        addListener { f ->
            if (f.isSuccess) {
                cont.resumeWith(Result.success(Unit))
            } else {
                cont.resumeWith(Result.failure(f.cause()))
            }
        }
    }
    return this
}

internal fun ByteBuf.toReadPacket(): ByteReadPacket {
    val buf = this
    return buildPacket {
        ByteBufInputStream(buf).withUse { copyTo(outputStream()) }
    }
}

internal fun ByteBuf.toByteArray(): ByteArray {
    val bytes = ByteArray(this.readableBytes())
    this.readBytes(bytes)
    return bytes
}

@OptIn(SorapointaInternal::class)
internal inline fun <reified T> ByteBuf.readToSoraPacket(
    key: ByteArray,
    block: (SoraPacket) -> T
) {
    if (this.readableBytes() > 12) { // TODO: why it should be 12
        val decrypt = this.toByteArray().xor(key).toReadPacket()
        if (decrypt.copy().readUShort() == START_MAGIC) {
            block(decrypt.readToSoraPacket())
        }
    }
}

internal fun ChannelOutboundInvoker.writeAndFlushOrCloseAsync(msg: Any?): ChannelFuture? {
    return writeAndFlush(msg)
        .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
        .addListener(ChannelFutureListener.CLOSE_ON_FAILURE)
}

internal fun buildMetadata(sequenceId: Int) =
    packetHead {
        clientSequenceId = sequenceId
        sentMs = now().toEpochMilliseconds()
    }
