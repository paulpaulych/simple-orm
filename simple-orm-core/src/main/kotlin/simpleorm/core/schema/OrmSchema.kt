package simpleorm.core.schema

import kotlin.reflect.KClass

data class OrmSchema(
        val entities: Map<KClass<*>, EntityDescriptor<out Any>>
){
    fun <T: Any> findEntityDescriptor(kClass: KClass<T>): EntityDescriptor<T> {
        val ed = entities[kClass]?:throw RuntimeException("Entity descriptor for class: ${kClass.qualifiedName} not found")
        return ed as EntityDescriptor<T>
    }
}

