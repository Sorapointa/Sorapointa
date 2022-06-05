package org.sorapinta.lua

import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.sorapointa.lua.LuaScriptEngine
import org.sorapointa.lua.luaToJVM
import kotlin.test.assertEquals

class LuaScriptEngineTest {
    @Test
    fun testEval() {
        val script = "print(hello) return 0"
        val context: MutableMap<String, Any> = HashMap()
        context["hello"] = "hello Lua"
        LuaScriptEngine.putAll(context)
        val result: Any = LuaScriptEngine.eval(script)
        Assertions.assertEquals(0L, result)
    }

    @Test
    fun testInvokeFunction() {
        val script = """testHello=function() print("hello") return 1 end"""
        LuaScriptEngine.eval(script)
        val result = LuaScriptEngine.invokeFunction("testHello")
        Assertions.assertEquals(1L, result)
    }

    @Test
    fun testAddFunction() {
        val script = "testAdd=function() return 1+2 end"
        LuaScriptEngine.eval(script)
        val result = LuaScriptEngine.invokeFunction("testAdd")
        Assertions.assertEquals(3L, result)
    }

    @Test
    fun testPassArgument() {
        val script = "returnSelf=function(a) return a end"
        LuaScriptEngine.eval(script)
        val param1 = 100L
        val result = LuaScriptEngine.invokeFunction("returnSelf", param1)
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
        LuaScriptEngine.eval("""printHello=function(a) print(a) end""")
        val impl = LuaScriptEngine.getInterface(TestInterface::class)!!
        impl.printHello("hello!")
    }

    @Test
    fun testInterfacePlusInvocable() {
        LuaScriptEngine.eval("""interfacePlus=function(a, b) return a + b end""")
        val impl = LuaScriptEngine.getInterface(TestInterface1::class)!!
        assertEquals(3, impl.interfacePlus(1, 2))
    }

    @Serializable
    data class SimpleDataForLua(
        val test: String,
        val long: Long,
    )

    @Test
    fun testSimpleData() {
        LuaScriptEngine.eval("testMetaTable=function(a) return a end")
        val data = SimpleDataForLua("100", 123)
        val result =
            LuaScriptEngine.invokeFunction("testMetaTable", data).luaToJVM<SimpleDataForLua>()
        assertEquals(data, result)
    }

    interface TestInterface2 {
        fun testInterfaceMetaTable(string: SimpleDataForLua): SimpleDataForLua
    }

    @Test
    fun testInterfaceSimpleData() {
        LuaScriptEngine.eval("testInterfaceMetaTable=function(a) return a end")
        val data = SimpleDataForLua("100", 123)
        val impl = LuaScriptEngine.getInterface(TestInterface2::class)!!
        assertEquals(data, impl.testInterfaceMetaTable(data))
    }
}
