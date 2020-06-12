package simpleorm.core.schema.property

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class ManyToOneProperty<T: Any> (
        kProperty: KProperty1<Any, T>,
        val kClass: KClass<T>,
        val manyIdProperty: KProperty1<Any, T>,
        val foreignKeyColumn: String
): PropertyDescriptor<T>(kProperty)
