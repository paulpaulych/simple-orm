package simpleorm

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

inline fun <reified T: Any> KClass<T>.getAll(): Collection<T>{
    val ormContext = OrmContextProvider.ormContext?: contextNotInitialized()
    val ormTemplate = ormContext.ormTemplate
    return ormTemplate.getAll(this)
}

inline fun <reified T: Any> KClass<T>.getByIdLazy(id: Any): T?{
    val ormTemplate = OrmContextProvider.ormContext?.ormTemplate ?: contextNotInitialized()
    return ormTemplate.getByIdLazy(this, id)
}

inline fun <reified T: Any> KClass<T>.getById(id: Any): T?{
    val ormTemplate = OrmContextProvider.ormContext?.ormTemplate ?: contextNotInitialized()
    return ormTemplate.getById(this, id)
}

inline fun <reified T: Any> KClass<T>.loadExtra(obj: T): T? {
    val ormTemplate = OrmContextProvider.ormContext?.ormTemplate ?: contextNotInitialized()
    return ormTemplate.loadExtra(this, obj)
}

inline fun <reified T: Any> KClass<T>.getByParam(params: Map<KProperty1<T,*>, Any?>): Collection<T>{
    val ormTemplate = OrmContextProvider.ormContext?.ormTemplate ?: contextNotInitialized()
    return ormTemplate.getByParam(this, params)
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