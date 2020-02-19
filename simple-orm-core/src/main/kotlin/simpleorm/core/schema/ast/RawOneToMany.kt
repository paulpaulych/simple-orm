package simpleorm.core.schema.ast

import kotlinx.serialization.Serializable

@Serializable
data class RawOneToMany(
    val className: String,
    val keyField: String
)