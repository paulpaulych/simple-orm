package simpleorm.core.schema

import simpleorm.core.schema.property.IdProperty
import simpleorm.core.schema.property.OneToManyProperty
import simpleorm.core.schema.property.PlainProperty
import simpleorm.core.schema.property.PropertyDescriptor
import kotlin.reflect.KProperty1

data class EntityDescriptor<T: Any> (
    val table: String,
    val properties: Map<KProperty1<T, *>, PropertyDescriptor>
){

    val plainProperties: Map<KProperty1<T, *>, PlainProperty>
        get() = properties.filter { it.value is PlainProperty }.map { it.key to it.value as PlainProperty }.toMap()

    val oneToManyProperties: Map<KProperty1<T, *>, OneToManyProperty>
        get() = properties.filter { it.value is OneToManyProperty }.map { it.key to it.value as OneToManyProperty }.toMap()

    //TODO: проверить что отношение property-column - биекция
    val propertyByColumn = plainProperties.map { it.value.column to it.key }.toMap()

    val idProperty: Pair<KProperty1<T, *>, IdProperty>

    init {
        val ids = properties.filter { it.value is IdProperty }
        if(ids.size != 1) error("expected ID properties count: 1, got: ${ids.size} ")
        idProperty =  ids.toList().first() as Pair<KProperty1<T, *>, IdProperty>
    }

}



