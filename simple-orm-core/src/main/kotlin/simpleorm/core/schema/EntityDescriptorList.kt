package simpleorm.core.schema

import kotlinx.serialization.Serializable

@Serializable
data class EntityDescriptorList(
    val entities: Map<String, EntityDescriptor>
)

@Serializable
data class EntityDescriptor(
    val table: String,
    val fields: Map<String, FieldDescriptor>
){
    val fieldByColumn: Map<String, String> = fields.map { it.value.column to it.key }.toMap()
}

@Serializable
data class FieldDescriptor(
    val isId: Boolean = false,
    val column: String
)


