package simpleorm.core.schema

import simpleorm.core.schema.naming.INamingStrategy
import kotlin.reflect.KClass

data class OrmSchema(
        val entities: Map<KClass<*>, EntityDescriptor<out Any>>,
        val namingStrategy: INamingStrategy
){
    fun <T: Any> findEntityDescriptor(kClass: KClass<T>): EntityDescriptor<T> {
        val ed = entities[kClass]?:throw DescriptorNotFoundException("Entity descriptor for class: ${kClass.qualifiedName} not found")
        return ed as EntityDescriptor<T>
    }
}

class DescriptorNotFoundException(message: String): RuntimeException(message)

