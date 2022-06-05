package org.sorapointa.lua.function

import net.sandius.rembulan.ByteString
import net.sandius.rembulan.LuaType
import org.sorapointa.lua.MetaTable
import org.sorapointa.lua.util.isUserData
import org.sorapointa.lua.util.luaType
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.typeOf

fun KFunction<*>.toLuaFunction(): ArgsFunction {
    val kfun = this@toLuaFunction
    return object : ArgsFunction() {
        override fun invoke(args: Array<out Any?>): Any? {
            require(kfun.parameters.size == args.size) {
                "KFunction and LuaFunction has different size [${kfun.parameters.size}] and [${args.size}]"
            }

            val convertedArgs = kfun.parameters.zip(other = args).map { (kParam, luaArg) ->
                val kType = kParam.type

                val convertedLua = when {
                    luaArg is ByteString -> luaArg.toRawString()
                    kType == typeOf<Int>() && luaArg is Long -> luaArg.toInt()
                    kType == typeOf<Float>() && luaArg is Double -> luaArg.toFloat()
                    else -> luaArg
                }

                when (kType.jvmErasure.luaType) {
                    LuaType.USERDATA -> (convertedLua as MetaTable<*>).exactObject
                    else -> convertedLua
                }
            }

            val result = kfun.call(args = convertedArgs.toTypedArray())

            return if (returnType == typeOf<Unit>()) null else {
                if (result.isUserData) MetaTable(result) else when (result) {
                    is String -> ByteString.of(result, Charsets.UTF_8)
                    else -> result
                }
            }
        }
    }
}
