package simpleorm.core.schema

import kotlinx.serialization.Serializable

@Serializable
data class EntityDescriptorList(
    val entities: Map<String, EntityDescriptor>
)

@Serializable
data class EntityDescriptor(
    val table: String,
    val columns: Map<String, String>
){
    val fields: Map<String, String> = columns.map { it.value to it.key }.toMap()
}

