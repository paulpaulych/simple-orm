package simpleorm.core.schema.yaml.ast

import kotlinx.serialization.Serializable

@Serializable
data class RawOrmSchema(
    val entities: Map<String, RawEntityDescriptor>
)
