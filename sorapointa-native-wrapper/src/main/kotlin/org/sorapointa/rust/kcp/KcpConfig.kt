package org.sorapointa.rust.kcp

data class KcpConfig(
    @JvmField val mtu: Long = 1400,
    @JvmField val noDelay: NoDelayConfig = NoDelayConfig.normal(),
    @JvmField val windowSize: WindowSize = WindowSize(),
    // seconds
    @JvmField val sessionExpire: Int = 90,
    @JvmField val flushWrite: Boolean = false,
    @JvmField val flushAckInput: Boolean = false,
    @JvmField val stream: Boolean = false,
)

data class WindowSize(
    @JvmField val first: Int = 256,
    @JvmField val second: Int = 256,
)

data class NoDelayConfig(
    @JvmField val noDelay: Boolean,
    @JvmField val interval: Int,
    @JvmField val resend: Int,
    @JvmField val nc: Boolean,
) {
    companion object {
        @Suppress("unused")
        fun fastest(): NoDelayConfig = NoDelayConfig(true, 10, 2, true)
        fun normal(): NoDelayConfig = NoDelayConfig(false, 40, 0, false)
    }
}
