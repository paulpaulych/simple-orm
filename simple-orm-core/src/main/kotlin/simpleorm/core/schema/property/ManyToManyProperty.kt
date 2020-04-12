package simpleorm.core.schema.property

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class ManyToManyProperty<T: Any>(
        kProperty: KProperty1<Any, T>,
        val kClass: KClass<T>,
        val linkTable: String,
        val leftColumn: String,
        val rightColumn: String,
        val rightKeyProperty: KProperty1<Any, *>
):PropertyDescriptor<T>(kProperty)