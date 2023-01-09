package org.sorapointa.game.data

import kotlinx.serialization.Serializable
import org.sorapointa.proto.Vector
import kotlin.math.sqrt

@Suppress("NOTHING_TO_INLINE")
inline fun Position(
    x: Int = 0,
    y: Int = 0,
    z: Int = 0
) = Position(x.toFloat(), y.toFloat(), z.toFloat())

@Suppress("unused", "MemberVisibilityCanBePrivate")
@Serializable
data class Position(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
) {

    fun set(x: Float = this.x, y: Float = this.y, z: Float = this.z) =
        Position(x = x, y = y, z = z)

    fun plus(x: Float = 0f, y: Float = 0f, z: Float = 0f) =
        Position(this.x + x, this.y + y, this.z + z)

    infix operator fun plus(addPos: Position) =
        Position(x + addPos.x, y + addPos.y, z + addPos.z)

    infix operator fun plus(all: Float) =
        Position(x + all, y + all, z + all)

    fun minus(x: Float = 0f, y: Float = 0f, z: Float = 0f) =
        Position(this.x + x, this.y + y, this.z + z)

    infix operator fun minus(subPos: Position) =
        Position(x - subPos.x, y - subPos.y, z - subPos.z)

    infix operator fun minus(all: Float) =
        Position(x - all, y - all, z - all)

    operator fun unaryMinus() =
        Position(0) - this

    fun times(x: Float = 1f, y: Float = 1f, z: Float = 1f) =
        Position(this.x * x, this.y * y, this.z * z)

    infix operator fun times(multiPos: Position) =
        Position(x * multiPos.x, y * multiPos.y, z * multiPos.z)

    infix operator fun times(all: Float) =
        Position(x * all, y * all, z * all)

    fun div(x: Float = 1f, y: Float = 1f, z: Float = 1f) =
        Position(this.x / x, this.y / y, this.z / z)

    infix operator fun div(divPos: Position) =
        Position(x / divPos.x, y / divPos.y, z / divPos.z)

    infix operator fun div(all: Float) =
        Position(x / all, y / all, z / all)

    fun min(pos: Position) =
        Position(kotlin.math.min(x, pos.x), kotlin.math.min(y, pos.y), kotlin.math.min(z, pos.z))

    fun max(pos: Position) =
        Position(kotlin.math.max(x, pos.x), kotlin.math.max(y, pos.y), kotlin.math.max(z, pos.z))

    fun clamp(upper: Position, lower: Position) =
        this.max(lower).min(upper)

    fun square() =
        (this * this).flatten()

    fun flatten() =
        x + y + z

    fun distance(pos: Position) =
        sqrt((this - pos).square())

    fun intersectWithCircle(
        rectangleA: Position,
        rectangleB: Position,
        circleCenter: Position,
        r: Float
    ): Boolean {
        val ext = (rectangleA - rectangleB) / 2f
        val rectangleCenter: Position = rectangleA - ext
        var d = circleCenter - rectangleCenter
        val clamped = d.clamp(ext, -ext)
        val p = rectangleCenter + clamped
        d = p - circleCenter
        return d.square() <= r * r
    }

    fun toProto() = Vector(x = x, y = y, z = z)

    override fun toString(): String =
        "Position[$x, $y, $z]"
}
