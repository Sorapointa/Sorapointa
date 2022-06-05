package org.sorapointa.lua

import net.sandius.rembulan.LuaType
import net.sandius.rembulan.runtime.LuaFunction
import org.sorapointa.lua.exception.LuaException
import org.sorapointa.lua.util.luaType
import java.io.BufferedReader
import java.io.Reader
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import javax.script.*
import kotlin.reflect.KClass

val globalLuaScope = LuaScriptEngine()

class LuaScriptEngine(
    val luaExecutor: LuaExecutor = LuaExecutor()
) : AbstractScriptEngine(), Invocable {

    fun putAll(bindings: Map<String, Any>) =
        bindings.forEach { (k, v) -> put(k, v) }

    override fun eval(script: String, context: ScriptContext): Any? {
        val luaContext: MutableMap<String, Any?> = HashMap()
        context.getBindings(ScriptContext.GLOBAL_SCOPE)?.let { luaContext.putAll(it) }
        context.getBindings(ScriptContext.ENGINE_SCOPE)?.let { luaContext.putAll(it) }

        return luaExecutor.runWithContext(script, luaContext).firstOrNull()
    }

    override fun eval(reader: Reader, context: ScriptContext): Any? {
        val script = reader.buffered().use(BufferedReader::readText)
        return eval(script, context)
    }

    override fun createBindings(): Bindings = SimpleBindings()

    override fun getFactory(): ScriptEngineFactory = LuaScriptEngineFactory

    fun invokeMethod(thiz: LuaScriptEngine, name: String, vararg args: Any?): Any? {
        val results = thiz.luaExecutor.call(name, args)
        return if (results?.size == 1) results.first() else results
    }

    override fun invokeMethod(thiz: Any?, name: String, vararg args: Any?): Any? {
        if (thiz !is LuaScriptEngine) {
            throw ScriptException("the target object is not a class or subclass of LuaScriptEngine")
        }
        return invokeMethod(thiz, name, args)
    }

    override fun invokeFunction(name: String, vararg args: Any?): Any? {
        if (luaExecutor.getFunction(name) == null) {
            val bindings = getBindings(ScriptContext.ENGINE_SCOPE)
            if (bindings?.get(name) is LuaFunction) {
                luaExecutor.putContext(bindings)
            } else throw LuaException("no such method called $name")
        }
        val results = luaExecutor.call(name, args = args)
        return if (results?.size == 1) results.first() else results
    }

    fun <T : Any> getInterface(clazz: KClass<T>): T? = getInterface(clazz.java)

    @Suppress("UNCHECKED_CAST")
    override fun <T> getInterface(clazz: Class<T>): T? =
        runCatching {
            clazz.declaredMethods.filter {
                luaExecutor.getFunction(it.name) == null
            }.forEach {
                val bindings = getBindings(ScriptContext.ENGINE_SCOPE)
                if (bindings?.get(it.name) is LuaFunction) {
                    luaExecutor.putContext(bindings)
                } else return null
            }
            Proxy.newProxyInstance(
                clazz.classLoader, arrayOf(clazz),
                LuaInvocationHandler(luaExecutor),
            ) as T
        }.getOrNull()

    override fun <T> getInterface(target: Any?, clazz: Class<T>): T? =
        (target as? LuaScriptEngine)?.getInterface(clazz)

    private class LuaInvocationHandler(private val executor: LuaExecutor) : InvocationHandler {
        override fun invoke(proxy: Any, method: Method, args: Array<out Any>): Any? {
            val result = executor.call(method.name, args = args)?.firstOrNull()

            if (method.returnType === Void.TYPE && result === null) return Unit

            return when (method.returnType.luaType) {
                LuaType.USERDATA -> (result as MetaTable<*>).exactObject
                else -> result
            }
        }
    }
}
