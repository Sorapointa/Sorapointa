package org.sorapointa.utils

class OptionalContainer<T>(
    private val defaultValue: T
) {

    var value: T = defaultValue
        private set

    var hasChanged: Boolean = false
        private set


    fun set(change: T?) {
        change?.also {
            value = it
            hasChanged = true
        }
    }

    fun reset() {
        this.value = defaultValue
    }

    inline fun <R> ifChanged(func: (T) -> R): R? {
        return if(hasChanged) func(value) else null
    }

}
