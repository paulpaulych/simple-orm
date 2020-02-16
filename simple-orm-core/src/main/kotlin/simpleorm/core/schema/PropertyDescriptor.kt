package simpleorm.core.schema

data class PropertyDescriptor (
    val column: String? = null,
    val isId: Boolean = false,
    val oneToMany: OneToMany<Any>? = null
)

