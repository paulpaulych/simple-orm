package simpleorm.core.schema

import simpleorm.core.schema.EntityDescriptor
import java.io.FileReader
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties

class EntityDescriptorRegistry(
    private val schemaDeserializer: SchemaDeserializer,
    private val schemaFileName: String
){

    private val entities: Map<KType, EntityDescriptor>

    init{
        val file = javaClass.classLoader.getResource(schemaFileName).file
        val readText = FileReader(file).readText()
        val rawSchemaInfo = schemaDeserializer.load(readText)

        entities = rawSchemaInfo.entities.map{
            val (clazz, entityInfo) = validate(it.key, it.value)
            clazz.createType() to entityInfo
        }.toMap()
    }

    fun validate(className: String, entityDescriptor: EntityDescriptor): Pair<KClass<Any>, EntityDescriptor>{
        val clazz = Class.forName(className).kotlin
        val declaredProperties = clazz.declaredMemberProperties
        val fieldsMap = entityDescriptor.fields.map { (propName, attrName) ->
            val prop = declaredProperties.find {it.name == propName }
            prop?: throw RuntimeException("unknown property: $propName")
            prop.name to attrName
        }.toMap()
        return (clazz as KClass<Any>) to EntityDescriptor(
            entityDescriptor.table,
            fieldsMap
        )
    }

    fun <T> findEntityDescriptor(type: KType): EntityDescriptor {
        return entities[type]?:throw RuntimeException("Entity descriptor for class: $type not found")
    }
}