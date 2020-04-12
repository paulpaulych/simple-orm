package simpleorm.core.schema.yaml.ast

import kotlinx.serialization.Serializable

@Serializable
data class RawFieldDescriptor(
    val isId: Boolean = false,
    val column: String? = null,
    val oneToMany: RawOneToMany? = null,
    val manyToMany: RawManyToMany? = null,
    val manyToOne: RawManyToOne? = null
)

