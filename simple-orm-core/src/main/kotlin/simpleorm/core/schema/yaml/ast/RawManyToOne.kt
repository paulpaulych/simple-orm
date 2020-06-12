package simpleorm.core.schema.yaml.ast

import kotlinx.serialization.Serializable

@Serializable
data class RawManyToOne (
        val className: String,
        val foreignKeyColumn: String
)