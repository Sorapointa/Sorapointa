package org.sorapointa.lua

import javax.script.ScriptEngine
import javax.script.ScriptEngineFactory

object LuaScriptEngineFactory : ScriptEngineFactory {
    private const val ENGINE_NAME = "Lua Crystal"
    private const val ENGINE_VERSION = "0.1"
    private const val LANGUAGE_NAME = "Lua"
    private const val LANGUAGE_VERSION = "5.3"
    private val ENGINE_EXTENSIONS = listOf("lua")
    private val ENGINE_MIME_TYPES = listOf("lua")
    private val ENGINE_NAMES = listOf("lua", "LUA", "Lua", "Crystal")
    private val THREADING: String? = null

    override fun getEngineName(): String = ENGINE_NAME

    override fun getEngineVersion(): String = ENGINE_VERSION

    override fun getExtensions(): List<String> = ENGINE_EXTENSIONS

    override fun getMimeTypes(): List<String> = ENGINE_MIME_TYPES

    override fun getNames(): List<String> = ENGINE_NAMES

    override fun getLanguageName(): String = LANGUAGE_NAME

    override fun getLanguageVersion(): String = LANGUAGE_VERSION

    override fun getParameter(key: String): Any? =
        when (key) {
            "ScriptEngine.ENGINE" -> engineName
            "ScriptEngine.ENGINE_VERSION" -> engineVersion
            "ScriptEngine.LANGUAGE" -> languageName
            "ScriptEngine.LANGUAGE_VERSION" -> languageVersion
            "ScriptEngine.NAME" -> names[0]
            "THREADING" -> THREADING
            else -> null
        }

    override fun getMethodCallSyntax(obj: String, m: String, vararg args: String): String? = null

    override fun getOutputStatement(toDisplay: String): String = "print($toDisplay)"

    override fun getProgram(vararg statements: String): String = statements.joinToString { " " }

    override fun getScriptEngine(): ScriptEngine = LuaScriptEngine()
}
