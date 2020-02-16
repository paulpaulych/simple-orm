package simpleorm.core.schema

import simpleorm.core.schema.ast.RawEntityDescriptor
import simpleorm.core.schema.ast.RawFieldDescriptor
import simpleorm.core.schema.ast.RawOrmSchema
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
    return PropertyDescriptor(
            column = this.column,
            isId = this.isId,
            manyToOne = this.manyToOne?.let { Class.forName(it).kotlin as KClass<Any> }
    )
}
