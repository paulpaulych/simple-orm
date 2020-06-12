package simpleorm.core.schema.yaml.ast

import kotlinx.serialization.Serializable

@Serializable
data class RawManyToMany(
        val className: String,
        val rightKeyField: String,
        val linkTable: String,
        val leftColumn: String,
        val rightColumn: String
)
