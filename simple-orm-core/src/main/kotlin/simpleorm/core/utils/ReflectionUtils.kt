package simpleorm.core.utils

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.declaredMemberProperties

fun <T: Any> KClass<T>.mutableProperties(): List<KMutableProperty1<T, *>>{
    return this.declaredMemberProperties.filterIsInstance<KMutableProperty1<T, *>>()
}

fun <T: Any> KClass<T>.immutableProperties(): List<KProperty1<T, *>>{
    return this.declaredMemberProperties.filter{ it !is KMutableProperty1<T, *> }
}

fun <T: Any> KClass<T>.property(name: String): KProperty1<T, *>{
    return this.declaredMemberProperties.find { it.name == name }
            ?: error("property $name not found in ${this.qualifiedName}")
}

fun <T: Any> KClass<T>.method(name: String): KFunction<*> {
    return this.declaredFunctions.find { it.name == name }
            ?: error("property $name not found in ${this.qualifiedName}")
}