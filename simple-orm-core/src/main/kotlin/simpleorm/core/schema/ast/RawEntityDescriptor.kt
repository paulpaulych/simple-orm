package simpleorm.core.schema.ast

import kotlinx.serialization.Serializable

@Serializable
data class RawEntityDescriptor(
    val table: String,
    val fields: Map<String, RawFieldDescriptor>
)