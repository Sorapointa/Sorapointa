package org.sorapointa.utils

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.sql.Column
import kotlin.reflect.KProperty

internal class SQLPropDelegate<TValue, TProp, TEntity : Entity<ID>, ID : Comparable<ID>>(
    private val entityData: TEntity,
    private val column: Column<TValue>,
    private val prop: TProp?,
    private val afterSetValue: (TProp, TValue) -> Unit
) {

    operator fun getValue(thisRef: TEntity, property: KProperty<*>): TValue =
        with(entityData) {
            column.getValue(thisRef, property)
        }

    operator fun setValue(thisRef: TEntity, property: KProperty<*>, value: TValue) =
        with(entityData) {
            column.setValue(thisRef, property, value)
            prop?.also { afterSetValue(prop, value) }
        }
}

internal class SetPropDelegate<TEntity, TValue, TProp>(
    private val prop: TProp?,
    private var propValue: TValue,
    private val afterSetValue: (TProp, TValue) -> Unit
) {

    operator fun getValue(thisRef: TEntity, property: KProperty<*>): TValue =
        propValue

    operator fun setValue(thisRef: TEntity, property: KProperty<*>, value: TValue) =
        prop?.also { afterSetValue(prop, value) }
}
