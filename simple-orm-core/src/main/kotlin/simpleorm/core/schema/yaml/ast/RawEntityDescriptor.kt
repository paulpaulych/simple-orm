package simpleorm.core.schema.yaml.ast

import kotlinx.serialization.Serializable

@Serializable
data class RawEntityDescriptor(
    val table: String,
    val fields: Map<String, RawFieldDescriptor>
)