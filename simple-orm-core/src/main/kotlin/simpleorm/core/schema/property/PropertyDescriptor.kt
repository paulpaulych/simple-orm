package simpleorm.core.schema.property

import kotlin.reflect.KProperty1

abstract class PropertyDescriptor<T: Any>(
    val kProperty: KProperty1<Any, T?>
)