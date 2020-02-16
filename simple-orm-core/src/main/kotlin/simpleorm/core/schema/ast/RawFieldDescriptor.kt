package simpleorm.core.schema.ast

import kotlinx.serialization.Serializable

@Serializable
data class RawFieldDescriptor(
    val isId: Boolean = false,
    val column: String,
    val manyToOne: String? = null
)