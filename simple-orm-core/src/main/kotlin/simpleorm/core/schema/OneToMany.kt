package simpleorm.core.schema

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

data class OneToMany<T: Any>(
    val kClass: KClass<T>,
    val foreignKey: KProperty1<T, *>
)