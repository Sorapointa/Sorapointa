package org.sorapointa.lua

import kotlinx.serialization.SerializationException
import kotlinx.serialization.serializer
import net.sandius.rembulan.ByteString
import net.sandius.rembulan.Table
import net.sandius.rembulan.Variable
import net.sandius.rembulan.compiler.CompilerChunkLoader
import net.sandius.rembulan.env.RuntimeEnvironments
import net.sandius.rembulan.exec.DirectCallExecutor
import net.sandius.rembulan.impl.StateContexts
import net.sandius.rembulan.lib.StandardLibrary
import net.sandius.rembulan.load.ChunkLoader
import net.sandius.rembulan.load.LoaderException
import net.sandius.rembulan.runtime.LuaFunction
import org.sorapointa.lua.exception.LuaException
import org.sorapointa.lua.util.isUserData
import org.sorapointa.utils.castOrNull
import org.sorapointa.utils.qualifiedOrSimple
import org.sorapointa.utils.readTextBuffered
import org.sorapointa.utils.uncheckedCast
import java.io.File
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createType

private const val ROOT_CLASS_PREFIX = "LUA_CLASSES"
private const val FUNCTION_NAME = "LUA_FUNCTION"

@Suppress("unused")
class LuaExecutor {
    /**
     * default context
     */
    private val state = StateContexts.newDefaultInstance()

    /**
     * all the context of the executor, actually key-value map
     * default context is standard library of lua 5.3, but compilation of code chunk may add new values
     */
    private val env = StandardLibrary.`in`(RuntimeEnvironments.system()).installInto(state)

    fun getEnv(key: String) = env.rawget(key)

    /**
     * default executor of Rembulan
     */
    private val executor = DirectCallExecutor.newExecutor()

    /**
     * run a code chunk
     *
     * @param script a code chunk to be compiled and called
     * @param args   optional parameters, but userdata will be converted to table in lua
     * @return results of the chunk if anything is returned or null
     */
    fun run(script: String, vararg args: Any?): Array<Any?> =
        if (script.isBlank()) emptyArray() else getObjects(script, args)

    private fun getObjects(script: String, args: Array<out Any?>): Array<Any?> {
        val loader: ChunkLoader = CompilerChunkLoader.of(ROOT_CLASS_PREFIX)
        return try {
            val function = loader.loadTextChunk(Variable(env), FUNCTION_NAME, script)
            executor.call(state, function, *convertArgs(*args))
        } catch (e: LoaderException) {
            throw LuaException("Lua run failed:", e.cause)
        }
    }

    suspend fun run(file: File, vararg args: Any?): Array<Any?> =
        run(file.readTextBuffered(), args)

    /**
     * @return nullable
     * @throws LuaException
     */
    fun call(functionName: String, vararg args: Any?): Array<out Any>? {
        val function: LuaFunction = getFunction(functionName)
            ?: throw LuaException("no such method called $functionName")
        return executor.call(state, function, *convertArgs(*args))
    }

    fun runWithContext(script: String, context: Map<String, Any?>, vararg args: Any?): Array<Any?> {
        context.forEach { (key, u) ->
            if (u.isUserData) {
                env.rawset(key, MetaTable(u))
            } else env.rawset(key, u)
        }
        return getObjects(script, args)
    }

    /**
     * get a lua function from executor's env
     *
     * @param name function's name
     */
    fun getFunction(name: String): LuaFunction? = env.rawget(name) as? LuaFunction

    /**
     * add context to executor's env
     *
     * @param context the key-value context add to env or replace existing value, userdata value will be
     * converted to table in lua
     */
    fun putContext(context: Map<String?, Any?>) {
        context.forEach { (key, _) ->
            try {
                env.rawset(key, MetaTable(context[key]))
            } catch (e: LuaException) {
                env.rawset(key, context[key])
            }
        }
    }

    /**
     * convert arguments to lua variable except userdata, userdata will be converted to table
     *
     * @param args arguments to be converted
     * @return converted arguments
     */
    private fun convertArgs(vararg args: Any?): Array<Any?> =
        args.map {
            if (it.isUserData) MetaTable(it) else it
        }.toTypedArray()
}

private inline fun <reified T : Any> MetaTable(value: T?): MetaTable<Any> {
    val clazz = value?.let { it::class }
    return MetaTable(value, clazz)
}

/**
 * Lua MetaTable
 */
class MetaTable<T : Any> internal constructor(
    value: T?,
    private val clazz: KClass<*>?,
) : Table() {

    private val serializer by lazy {
        clazz?.let { serializer(it.createType()) }
    }

    /**
     * to store a table whose key type is **Integer** or **Long** in Java
     */
    private val longKeyTable = TreeMap<Long, Any?>()

    /**
     * to store a table whose key type is **Double** or **Float** in Java
     */
    private val doubleKeyTable = TreeMap<Double, Any?>()

    /**
     * to store a table whose key type is **string** in Lua
     */
    private val stringKeyTable = TreeMap<String, Any?>()

    /**
     * to store a table whose key type is **Boolean** in Java
     */
    private val boolKeyTable = TreeMap<Boolean, Any?>()

    /**
     * for extension, not implemented
     */
    private val anyTable = TreeMap<Any, Any?>()

    private fun checkedSerializer() =
        serializer ?: throw SerializationException("Serializer for class '${clazz?.qualifiedOrSimple}' is not found")

    init {
        when (value) {
            null -> {
                // do nothing
            }
            is Array<*> -> value.toList().buildMetaTable()
            is Collection<*> -> value.buildMetaTable()
            is Map<*, *> -> value.forEach { any, mapValue ->
                any?.also { insertIntoTable(it, mapValue) }
            }
            else -> {
                val json = luaJson.encodeToJsonElement(checkedSerializer(), value)
                val table = json.toLuaTable().castOrNull<Map<String, *>>()
                table?.forEach { (k, v) ->
                    insertIntoTable(k, v)
                }
            }
        }
    }

    private fun Collection<*>.buildMetaTable() {
        forEachIndexed { idx, node ->
            // lua index starts from 1
            insertIntoLongTable(idx + 1L, node)
        }
    }

    val exactObject: T? by lazy { toExactObject() }

    fun <T : Any> toExactObject(): T? {
        val jsonElement = stringKeyTable.toJsonElement()
        return luaJson.decodeFromJsonElement(checkedSerializer(), jsonElement).uncheckedCast()
    }

    override fun setMode(weakKeys: Boolean, weakValues: Boolean) {
    }

    fun toList(): List<T> = longKeyTable.values.toList().uncheckedCast()

    private fun insertIntoTable(key: Any, value: Any?): Any? =
        when (key) {
            is Long -> insertIntoLongTable(key, value)
            is Int -> insertIntoLongTable(key.toLong(), value)
            is Double -> insertIntoDoubleTable(key, value)
            is Float -> insertIntoDoubleTable(key.toDouble(), value)
            is String -> insertIntoStringTable(key, value)
            is ByteString -> insertIntoStringTable(key.toRawString(), value)
            is Boolean -> insertIntoBoolKeyTable(key, value)
            else -> anyTable[key] = value
        }

    private fun insertIntoLongTable(key: Long, value: Any?) =
        if (value.isUserData) {
            longKeyTable.put(key, MetaTable(value))
        } else longKeyTable.put(key, value)

    private fun insertIntoDoubleTable(key: Double, value: Any?) =
        if (value.isUserData) {
            doubleKeyTable.put(key, MetaTable(value))
        } else doubleKeyTable.put(key, value)

    private fun insertIntoStringTable(key: String, value: Any?) =
        if (value.isUserData) {
            stringKeyTable.put(key, MetaTable(value))
        } else stringKeyTable.put(key, value)

    private fun insertIntoBoolKeyTable(key: Boolean, value: Any?) =
        if (value.isUserData) {
            boolKeyTable.put(key, MetaTable(value))
        } else boolKeyTable.put(key, value)

    private fun getFromTable(key: Any): Any? =
        when (key) {
            is Long -> longKeyTable[key]
            is Int -> longKeyTable[key.toLong()]
            is Double -> doubleKeyTable[key]
            is Float -> doubleKeyTable[key.toDouble()]
            is String -> stringKeyTable[key]
            is Boolean -> boolKeyTable[key]
            else -> anyTable[key]
        }

    override fun rawget(key: Any?): Any? = key?.let { getFromTable(it) }

    override fun rawset(key: Any?, value: Any?) {
        insertIntoTable(key ?: return, value)
    }

    override fun initialKey(): Any? =
        if (!longKeyTable.isEmpty()) {
            longKeyTable.firstKey()
        } else if (!stringKeyTable.isEmpty()) {
            stringKeyTable
        } else null

    override fun successorKeyOf(key: Any?): Any? =
        when (key) {
            is Long -> {
                if (longKeyTable.lastKey() == key) {
                    doubleKeyTable.keys.firstOrNull()
                        ?: stringKeyTable.keys.firstOrNull()
                        ?: boolKeyTable.keys.firstOrNull()
                } else longKeyTable.higherKey(key)
            }
            is Double -> {
                if (doubleKeyTable.lastKey() == key) {
                    stringKeyTable.keys.firstOrNull()
                        ?: boolKeyTable.keys.firstOrNull()
                } else doubleKeyTable.higherKey(key)
            }
            is String -> {
                if (stringKeyTable.lastKey() == key) {
                    boolKeyTable.keys.firstOrNull()
                } else stringKeyTable.higherKey(key)
            }
            is Boolean -> if (boolKeyTable.lastKey() == key) null else boolKeyTable.higherKey(key)
            else -> null
        }
}
