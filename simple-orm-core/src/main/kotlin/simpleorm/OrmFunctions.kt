package simpleorm

import kotlin.reflect.KClass

inline fun <reified T: Any> KClass<T>.getAll(): Collection<T>{
    val ormContext = OrmContextProvider.ormContext?: contextNotInitialized()
    val ormTemplate = ormContext.ormTemplate
    return ormTemplate.getAll(this)
}

inline fun <reified T: Any> KClass<T>.getById(id: Any): T?{
    val ormTemplate = OrmContextProvider.ormContext?.ormTemplate ?: contextNotInitialized()
    return ormTemplate.getById(this, id)
}

inline fun <reified T: Any> save(obj: T){
    val ormTemplate = OrmContextProvider.ormContext?.ormTemplate ?: contextNotInitialized()
    return ormTemplate.save(T::class, obj)
}

inline fun <reified T: Any> saveAll(values: Collection<T>){
    val ormTemplate = OrmContextProvider.ormContext?.ormTemplate ?: contextNotInitialized()
    return ormTemplate.saveAll(T::class, values)
}

fun contextNotInitialized(): Nothing{
    error("orm context is not yet initialized")
}