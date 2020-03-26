package simpleorm.core.schema.property

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class OneToManyProperty<T: Any>(
        kProperty: KProperty1<Any, T>,
        val kClass: KClass<T>,
        val foreignKey: KProperty1<Any, *>
): PropertyDescriptor<T>(kProperty)