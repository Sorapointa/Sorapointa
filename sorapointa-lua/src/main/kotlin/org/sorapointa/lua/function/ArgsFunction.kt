package org.sorapointa.lua.function

import net.sandius.rembulan.impl.NonsuspendableFunctionException
import net.sandius.rembulan.runtime.ExecutionContext
import net.sandius.rembulan.runtime.LuaFunction

abstract class ArgsFunction : LuaFunction() {
    final override fun resume(context: ExecutionContext, suspendedState: Any?) =
        throw NonsuspendableFunctionException(javaClass)

    final override fun invoke(context: ExecutionContext) =
        invoke(context, args = emptyArray())

    final override fun invoke(context: ExecutionContext, arg1: Any?) =
        invoke(context, args = arrayListOf(arg1).toTypedArray())

    final override fun invoke(context: ExecutionContext, arg1: Any?, arg2: Any?) =
        invoke(context, args = arrayListOf(arg1, arg2).toTypedArray())

    final override fun invoke(context: ExecutionContext, arg1: Any?, arg2: Any?, arg3: Any?) =
        invoke(context, args = arrayListOf(arg1, arg2, arg3).toTypedArray())

    final override fun invoke(context: ExecutionContext, arg1: Any?, arg2: Any?, arg3: Any?, arg4: Any?) =
        invoke(context, args = arrayListOf(arg1, arg2, arg3, arg4).toTypedArray())

    final override fun invoke(context: ExecutionContext, arg1: Any?, arg2: Any?, arg3: Any?, arg4: Any?, arg5: Any?) =
        invoke(context, args = arrayListOf(arg1, arg2, arg3, arg4, arg5).toTypedArray())

    final override fun invoke(context: ExecutionContext, args: Array<out Any?>) {
        val result = invoke(args)
        context.returnBuffer.setToContentsOf(arrayOf(result))
    }

    abstract fun invoke(args: Array<out Any?>): Any?
}
