package org.sorapinta.lua

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.sandius.rembulan.runtime.AbstractFunction0
import net.sandius.rembulan.runtime.ExecutionContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.sorapointa.lua.MetaTable
import org.sorapointa.lua.function.toLuaFunction
import org.sorapointa.lua.globalLuaScope
import org.sorapointa.lua.util.luaToJVM
import kotlin.test.assertEquals

class LuaScriptEngineTest {
    @Test
    fun testEval() {
        val script = "print(hello) return 0"
        val context: MutableMap<String, Any> = HashMap()
        context["hello"] = "hello Lua"
        globalLuaScope.putAll(context)
        val result: Any = globalLuaScope.eval(script)
        Assertions.assertEquals(0L, result)
    }

    @Test
    fun testInvokeFunction() {
        val script = """testHello=function() print("hello") return 1 end"""
        globalLuaScope.eval(script)
        val result = globalLuaScope.invokeFunction("testHello")
        Assertions.assertEquals(1L, result)
    }

    @Test
    fun testAddFunction() {
        val script = "testAdd=function() return 1+2 end"
        globalLuaScope.eval(script)
        val result = globalLuaScope.invokeFunction("testAdd")
        Assertions.assertEquals(3L, result)
    }

    @Test
    fun testPassArgument() {
        val script = "returnSelf=function(a) return a end"
        globalLuaScope.eval(script)
        val param1 = 100L
        val result = globalLuaScope.invokeFunction("returnSelf", param1)
        Assertions.assertEquals(100L, result)
    }

    interface TestInterface {
        fun printHello(string: String)
    }

    interface TestInterface1 {
        fun interfacePlus(a: Int, b: Int): Long
    }

    @Test
    fun testInterfaceInvocable() {
        globalLuaScope.eval("""printHello=function(a) print(a) end""")
        val impl = globalLuaScope.getInterface(TestInterface::class)!!
        impl.printHello("hello!")
    }

    @Test
    fun testInterfacePlusInvocable() {
        globalLuaScope.eval("""interfacePlus=function(a, b) return a + b end""")
        val impl = globalLuaScope.getInterface(TestInterface1::class)!!
        assertEquals(3, impl.interfacePlus(1, 2))
    }

    @Serializable
    data class SimpleDataForLua(
        val test: String,
        val long: Long,
    )

    @Test
    fun testSimpleData() {
        globalLuaScope.eval("testMetaTable=function(a) return a end")
        val data = SimpleDataForLua("100", 123)
        val result =
            globalLuaScope.invokeFunction("testMetaTable", data).luaToJVM<SimpleDataForLua>()
        assertEquals(data, result)
    }

    interface TestInterface2 {
        fun testInterfaceMetaTable(string: SimpleDataForLua): SimpleDataForLua
    }

    @Test
    fun testInterfaceSimpleData() {
        globalLuaScope.eval("testInterfaceMetaTable=function(a) return a end")
        val data = SimpleDataForLua("100", 123)
        val impl = globalLuaScope.getInterface(TestInterface2::class)!!
        assertEquals(data, impl.testInterfaceMetaTable(data))
    }

    @Test
    fun luaCallJVM() {
        var local = 100

        val invocable = object : AbstractFunction0() {
            override fun resume(context: ExecutionContext?, suspendedState: Any?) = TODO()

            override fun invoke(context: ExecutionContext?) {
                local++
                println("Call from Lua!")
            }
        }

        globalLuaScope.putAll(mapOf("callJVM" to invocable))

        globalLuaScope.eval("callJVM()")

        assertEquals(101, local)
    }

    @Test
    fun returnFromLua() {

        @Serializable
        data class ParticleShapeType(
            @SerialName("Volume") var volume: Int? = null,
            @SerialName("Edge") var edge: Int? = null,
            @SerialName("Shell") var shell: Int? = null,
        )

        globalLuaScope.eval(
            """
            ConfigCommon=function(a)
                print(a.Volume)
                a.Volume = 0
                print(a.Volume)
                a.Edge = 1
                print(a.Edge)
                a.Shell = 2
                print(a.Shell)
                return a
            end
            """.trimIndent()
        )
        val result = globalLuaScope.invokeFunction("ConfigCommon", ParticleShapeType()) as MetaTable<*>
        assertEquals(ParticleShapeType(0, 1, 2), result.exactObject)
    }

    fun repeat(string: String, repeat: Int): String = string.repeat(repeat)

    @Test
    fun dynamicGenerate() {
        globalLuaScope.put("test", ::repeat.toLuaFunction())
        globalLuaScope.eval("""luaTest=function() return test("123123", 10) end""".trimIndent())
        globalLuaScope.invokeFunction("luaTest").also {
            println(it)
        }
    }
}
