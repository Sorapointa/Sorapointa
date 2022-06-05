package org.sorapointa.lua.util

import net.sandius.rembulan.ByteString
import net.sandius.rembulan.LuaType
import net.sandius.rembulan.Table
import net.sandius.rembulan.runtime.Coroutine
import net.sandius.rembulan.runtime.LuaFunction
import org.sorapointa.lua.MetaTable
import org.sorapointa.utils.uncheckedCast
import kotlin.reflect.KClass

inline fun <reified T : Any> Any?.luaToJVM(): T? = (this.uncheckedCast<MetaTable<T>>()).exactObject

internal inline val Any?.luaType: LuaType get() = LuaType.typeOf(this)

internal inline val Any?.isUserData: Boolean get() = luaType == LuaType.USERDATA

internal val Class<*>?.luaType
    get() = this?.kotlin.luaType

internal val KClass<*>?.luaType
    get() = when (this) {
        null -> LuaType.NIL
        Boolean::class -> LuaType.BOOLEAN
        Int::class -> LuaType.NUMBER
        Long::class -> LuaType.NUMBER
        Float::class -> LuaType.NUMBER
        Double::class -> LuaType.NUMBER
        ByteString::class -> LuaType.STRING
        String::class -> LuaType.STRING
        Table::class -> LuaType.TABLE
        LuaFunction::class -> LuaType.FUNCTION
        Coroutine::class -> LuaType.THREAD
        else -> LuaType.USERDATA
    }
