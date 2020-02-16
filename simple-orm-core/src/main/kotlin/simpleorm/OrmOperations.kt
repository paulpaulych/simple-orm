package simpleorm

import kotlin.reflect.KClass

interface OrmOperations{

    fun <T: Any> getAll(kClass: KClass<T>): Collection<T>

    fun <T: Any> getById(kClass: KClass<T>, id: Any): T?

    fun <T: Any> save(kClass: KClass<T>, obj: T)

    fun <T: Any> saveAll(kClass: KClass<T>, obj: Collection<T>)

}