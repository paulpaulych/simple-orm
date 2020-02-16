package simpleorm

import kotlin.reflect.KClass

inline fun <reified T: Any> KClass<T>.getAll(): Collection<T>{
    val ormContext = OrmContextProvider.ormContext?: error("orm context is not yet initialized")
    val ormTemplate = ormContext.ormTemplate
    return ormTemplate.getAll(getOuter(this.java)) as Collection<T>
}

inline fun <reified T: Any> KClass<T>.getById(id: Any): T?{
    val ormContext = OrmContextProvider.ormContext?: error("orm context is not yet initialized")
    val ormTemplate = ormContext.ormTemplate
    return ormTemplate.getById(getOuter(this.java), id) as T?
}

//inline fun <reified T> KClass<T>.getById(constraints: Map<String, Any>): Collection<T>{
//    val ormTemplate = OrmContextProvider.ormContext?.ormTemplate ?: error("orm context is not yet initialized")
//    return ormTemplate.executeParametrizedSelect(
//            getOuter(T::class.java),
//            constraints.map{it.key to it.value.toString()}.toMap()
//    ) as Collection<T>
//}

inline fun <reified T: Any> save(obj: T){
    val ormTemplate = OrmContextProvider.ormContext?.ormTemplate ?: error("orm context is not yet initialized")
    return ormTemplate.save(T::class, obj)
}

inline fun <reified T: Any> saveAll(values: Collection<T>){
    val ormTemplate = OrmContextProvider.ormContext?.ormTemplate ?: error("orm context is not yet initialized")
    return ormTemplate.saveAll(T::class, values)
}

fun <T> getOuter(companion: Class<T>): KClass<out Any>{
    val newClassName = companion.canonicalName?.replace(".Companion", "") ?: throw RuntimeException("cannot get qualified name")
    return Class.forName(newClassName).kotlin
}