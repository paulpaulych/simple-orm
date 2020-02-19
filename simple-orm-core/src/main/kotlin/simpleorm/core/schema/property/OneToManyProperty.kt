package simpleorm.core.schema.property

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

data class OneToManyProperty(
        val kClass: KClass<Any>,
        val foreignKey: KProperty1<Any, *>
): PropertyDescriptor