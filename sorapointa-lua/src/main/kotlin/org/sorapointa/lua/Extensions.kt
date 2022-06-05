package org.sorapointa.lua

import net.sandius.rembulan.ByteString
import net.sandius.rembulan.LuaType
import net.sandius.rembulan.Table
import net.sandius.rembulan.runtime.Coroutine
import net.sandius.rembulan.runtime.LuaFunction
import org.sorapointa.utils.uncheckedCast

inline fun <reified T : Any> Any?.luaToJVM(): T? = (this.uncheckedCast<MetaTable<T>>()).exactObject

internal inline val Any?.luaType: LuaType get() = LuaType.typeOf(this)

internal inline val Any?.isUserData: Boolean get() = luaType == LuaType.USERDATA

internal val Class<*>?.luaType
    get() = when (this) {
        null -> LuaType.NIL
        Boolean::class.java -> LuaType.BOOLEAN
        Int::class.java -> LuaType.NUMBER
        Long::class.java -> LuaType.NUMBER
        Float::class.java -> LuaType.NUMBER
        Double::class.java -> LuaType.NUMBER
        ByteString::class.java -> LuaType.STRING
        String::class.java -> LuaType.STRING
        Table::class.java -> LuaType.TABLE
        LuaFunction::class.java -> LuaType.FUNCTION
        Coroutine::class.java -> LuaType.THREAD
        else -> LuaType.USERDATA
    }
