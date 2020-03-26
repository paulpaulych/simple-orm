package simpleorm.core.delegate

import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Provides method for both ReadWriteProperty and ReadOnlyProperty
 */
interface GenericDelegate<T>: ReadWriteProperty<Any, T?>, ReadOnlyProperty<Any, T?> {

    override fun getValue(thisRef: Any, property: KProperty<*>): T?

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?)

}