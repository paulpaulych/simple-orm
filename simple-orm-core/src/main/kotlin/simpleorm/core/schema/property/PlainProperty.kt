package simpleorm.core.schema.property

import kotlin.reflect.KProperty1

open class PlainProperty<T: Any>(
        kProperty: KProperty1<Any, T>,
        val column: String
): PropertyDescriptor<T>(kProperty)

