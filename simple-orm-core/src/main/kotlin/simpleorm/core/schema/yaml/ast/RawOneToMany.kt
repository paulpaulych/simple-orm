package simpleorm.core.schema.yaml.ast

import kotlinx.serialization.Serializable

@Serializable
data class RawOneToMany(
    val className: String,
    val keyField: String
)