package simpleorm.core.schema.ast

import kotlinx.serialization.Serializable

@Serializable
data class RawOrmSchema(
    val entities: Map<String, RawEntityDescriptor>
)
