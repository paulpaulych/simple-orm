package simpleorm.core.schema

import kotlin.reflect.KClass

data class PropertyDescriptor (
    val column: String,
    val isId: Boolean = false,
    val manyToOne: KClass<Any>? = null
)