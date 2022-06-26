package org.sorapointa.utils

import io.ktor.utils.io.core.*
import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelOutboundInvoker
import kotlinx.coroutines.suspendCancellableCoroutine
import mu.KotlinLogging
import org.sorapointa.proto.START_MAGIC
import org.sorapointa.proto.SoraPacket
import org.sorapointa.proto.packetHead
import org.sorapointa.proto.readToSoraPacket
import org.sorapointa.utils.encoding.hex
import java.net.InetSocketAddress

private val logger = KotlinLogging.logger {}

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

internal fun ByteBuf.toByteArray(): ByteArray {
    val bytes = ByteArray(this.readableBytes())
    this.readBytes(bytes)
    return bytes
}

internal inline fun <reified T> ByteBuf.readToSoraPacket(
    key: ByteArray,
    block: (SoraPacket) -> T
) {
    if (this.readableBytes() > 12) { // 2+2+2+4+2 at least to be > 12 bytes
        val before = this.copy().toByteArray()
        val decrypt = this.toByteArray().xor(key).toReadPacket()
        if (decrypt.copy().readUShort() == START_MAGIC) {
            block(decrypt.readToSoraPacket())
        } else {
            logger.debug { "Detected insanity packet, hex: ${before.hex} decrypted: ${decrypt.readBytes().hex}" }
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
        sentMs = nowMilliseconds()
    }
