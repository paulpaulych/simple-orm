package simpleorm.core.schema

import kotlin.reflect.KProperty1

data class EntityDescriptor<T: Any> (
    val table: String,
    val properties: Map<KProperty1<T, *>, PropertyDescriptor>
){
    //TODO: проверить что отношение property-column - биекция
    val propertyByColumn = properties.map { it.value.column to it.key }.toMap()

    val id: KProperty1<T, *> = properties.toList().find { it.second.isId }?.first
            ?: error("id is not specified for table $table")
}
