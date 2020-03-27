package simpleorm.core.schema.yaml.ast

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import simpleorm.core.schema.EntityDescriptor
import simpleorm.core.schema.OrmSchema
import simpleorm.core.schema.SchemaCreator
import simpleorm.core.schema.property.IdProperty
import simpleorm.core.schema.property.OneToManyProperty
import simpleorm.core.schema.property.PlainProperty
import simpleorm.core.schema.property.PropertyDescriptor
import simpleorm.core.utils.property
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

class YamlSchemaCreator(
        private val rawText: String
): SchemaCreator {

    override fun create(): OrmSchema {
        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule())
        val raw = mapper.readValue(rawText, RawOrmSchema::class.java)
        return convertRawOrmSchema(raw)
    }

    private fun convertRawOrmSchema(rawOrmSchema: RawOrmSchema): OrmSchema{
        return OrmSchema(
            rawOrmSchema.entities.map{ (className, rawEntityDescriptor) ->
                val entityClass = Class.forName(className).kotlin
                return@map entityClass to convertRawEntityDescriptor(entityClass, rawEntityDescriptor)
            }.toMap()
        )
    }

    private fun <T: Any> convertRawEntityDescriptor(kClass: KClass<T>, raw: RawEntityDescriptor): EntityDescriptor<T> {
        return EntityDescriptor(
                kClass,
                raw.table,
                raw.fields.map{(propName, rawPd)->
                    val kProperty = kClass.declaredMemberProperties.find {it.name == propName}
                            ?: error("property with name '$propName' not found in class $kClass")
                    return@map kProperty to convertRawPropertyDescriptor(kProperty, rawPd)
                }.toMap()
        )
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
            val manyClass = Class.forName(raw.oneToMany.className).kotlin as KClass<Any>
            return OneToManyProperty(
                    kProperty as KProperty1<Any, Any>,
                    manyClass,
                    manyClass.property(raw.oneToMany.keyField)
            )
        }
        error("invalid schema")
    }

}
