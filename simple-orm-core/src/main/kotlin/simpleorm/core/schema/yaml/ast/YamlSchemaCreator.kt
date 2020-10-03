@file:Suppress("UNCHECKED_CAST")
package simpleorm.core.schema.yaml.ast

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import simpleorm.core.schema.EntityDescriptor
import simpleorm.core.schema.OrmSchema
import simpleorm.core.schema.SchemaCreator
import simpleorm.core.schema.naming.INamingStrategy
import simpleorm.core.schema.property.*
import simpleorm.core.utils.property
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

class YamlSchemaCreator(
        private val rawText: String,
        private val namingStrategy: INamingStrategy
): SchemaCreator {

    private val eds = mutableListOf<EntityDescriptor<*>>()

    override fun create(): OrmSchema {
        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule())
        val raw = mapper.readValue(rawText, RawOrmSchema::class.java)
        return convertRawOrmSchema(raw)
    }

    private fun convertRawOrmSchema(rawOrmSchema: RawOrmSchema): OrmSchema{
        return OrmSchema(
            rawOrmSchema.entities.map{ (className, rawEntityDescriptor) ->
                val entityClass = classForName(className)
                return@map entityClass to convertRawEntityDescriptor(entityClass, rawEntityDescriptor)
            }.toMap(),
            namingStrategy
        )
    }

    private fun <T: Any> convertRawEntityDescriptor(kClass: KClass<T>, raw: RawEntityDescriptor): EntityDescriptor<T> {
        val ed = EntityDescriptor(
                kClass,
                raw.table,
                raw.fields.map{(propName, rawPd)->
                    val kProperty = kClass.declaredMemberProperties.find {it.name == propName}
                            ?: error("property with name '$propName' not found in class $kClass")
                    return@map kProperty to convertRawPropertyDescriptor(kProperty, rawPd)
                }.toMap()
        )
        eds.add(ed)
        return ed
    }

    private fun <R: Any> convertRawPropertyDescriptor(kProperty: KProperty1<R, *>, raw: RawFieldDescriptor): PropertyDescriptor<Any> {
        if(raw.isId){
            return IdProperty(
                    kProperty as KProperty1<Any, Any>,
                    raw.column!!)
        }
        if(raw.column != null){
            return PlainProperty(
                    kProperty as KProperty1<Any, Any>,
                    raw.column
            )
        }
        if(raw.oneToMany != null){
            val manyClass = classForName(raw.oneToMany.className) as KClass<Any>
            return OneToManyProperty(
                    kProperty as KProperty1<Any, Any>,
                    manyClass,
                    manyClass.property(raw.oneToMany.keyField)
            )
        }
        if(raw.manyToMany != null){
            val rightClass = classForName(raw.manyToMany.className) as KClass<Any>
            return ManyToManyProperty(
                    kProperty as KProperty1<Any, Any>,
                    rightClass,
                    raw.manyToMany.linkTable,
                    raw.manyToMany.leftColumn,
                    raw.manyToMany.rightColumn,
                    rightClass.property(raw.manyToMany.rightKeyField)
            )
        }
        if(raw.manyToOne != null){
            val kClass = classForName(raw.manyToOne.className) as KClass<Any>
            val manyIdProperty = eds.find { it.kClass == kClass }?.idProperty
                    ?: error("please describe $kClass higher in schema file")
            return ManyToOneProperty(
                    kProperty as KProperty1<Any, Any>,
                    kClass,
                    manyIdProperty.kProperty as KProperty1<Any, Any>,
                    raw.manyToOne.foreignKeyColumn
            )
        }
        error("invalid schema")
    }

    private fun classForName(name: String): KClass<*> =
        try {
            Class.forName(name).kotlin
        } catch (e: ClassNotFoundException){
            error("class not found: ${e.message}")
        }

}
