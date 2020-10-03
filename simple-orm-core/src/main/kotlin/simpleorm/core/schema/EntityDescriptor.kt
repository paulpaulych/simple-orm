@file:Suppress("UNCHECKED_CAST")
package simpleorm.core.schema

import simpleorm.core.schema.property.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

data class EntityDescriptor<T: Any>(
        val kClass: KClass<T>,
        val table: String,
        val properties: Map<KProperty1<T, *>, PropertyDescriptor<*>>
){

    val idProperty: IdProperty<*>

    val plainProperties = properties
            .filter { it.value is PlainProperty<*> }
            .mapValues { it.value as PlainProperty<Any> }
            .mapKeys { it.key as KProperty1<Any, *> }

    val oneToManyProperties = properties
            .filter { it.value is OneToManyProperty<*> }
            .mapValues { it.value as OneToManyProperty<Any> }
            .mapKeys { it.key as KProperty1<Any, *> }

    val manyToManyProperties = properties
            .filter { it.value is ManyToManyProperty<*> }
            .mapValues { it.value as ManyToManyProperty<Any> }
            .mapKeys { it.key as KProperty1<Any, *> }

    val manyToOneProperties = properties
            .filter { it.value is ManyToOneProperty<*> }
            .mapValues { it.value as ManyToOneProperty<Any> }
            .mapKeys { it.key as KProperty1<Any, *> }


    init{
        val rawIdProp = properties.values.find{ it is IdProperty}
                ?: error("id property not specified for $kClass")
        idProperty = rawIdProp as IdProperty<*>
    }

    fun getPropertyDescriptor(kProperty: KProperty1<*, *>): PropertyDescriptor<*> {
        return properties[kProperty]
                ?: error("property descriptor not found")
    }

}



