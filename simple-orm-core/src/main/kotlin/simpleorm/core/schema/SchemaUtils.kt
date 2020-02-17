package simpleorm.core.schema

import simpleorm.core.schema.ast.RawEntityDescriptor
import simpleorm.core.schema.ast.RawFieldDescriptor
import simpleorm.core.schema.ast.RawOneToMany
import simpleorm.core.schema.ast.RawOrmSchema
import simpleorm.core.schema.property.IdProperty
import simpleorm.core.schema.property.OneToManyProperty
import simpleorm.core.schema.property.PlainProperty
import simpleorm.core.schema.property.PropertyDescriptor
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

fun RawOrmSchema.toOrmSchema(): OrmSchema {
    return OrmSchema(
            entities = this.entities.map { (kClassName, rawEntityDescriptor) ->
                val kClass = Class.forName(kClassName).kotlin as KClass<Any>
                kClass to rawEntityDescriptor.toEntityDescriptor(kClass)
            }.toMap()
    )
}

private fun RawEntityDescriptor.toEntityDescriptor(kClass: KClass<Any>): EntityDescriptor<Any> {
    return EntityDescriptor(
            table = this.table,
            properties = this.fields.map { (fieldName, rawFieldDescriptor) ->
                val kProperty = kClass.declaredMemberProperties.find { it.name == fieldName }
                        ?: error("unknown property: $fieldName")
                kProperty to rawFieldDescriptor.toPropertyDescriptor()
            }.toMap()
    )
}

private fun RawFieldDescriptor.toPropertyDescriptor(): PropertyDescriptor {

    if(this.oneToMany != null){
        val kClass = Class.forName(oneToMany.className).kotlin as KClass<Any>
        val kProperty = kClass.declaredMemberProperties.find { it.name == oneToMany.keyField }
                ?: error("property ${oneToMany.keyField} not found in ${kClass.qualifiedName}")
        return OneToManyProperty(
                kClass = kClass,
                foreignKey = kProperty
        )
    }
    if(this.isId){
        return IdProperty(
            column = this.column?: error("column for property TODO() not specified")
        )
        //TODO: вставить название проперти
    }
    if(this.column != null){
        return PlainProperty(column)
    }
    error("property kind not determined")
}
