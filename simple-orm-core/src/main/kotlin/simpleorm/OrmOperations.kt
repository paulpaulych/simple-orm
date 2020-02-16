package simpleorm

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface OrmOperations{

    fun <T: Any> getAll(kClass: KClass<T>): Collection<T>

    fun <T: Any> getByParam(kClass: KClass<T>, params: Map<KProperty1<T,*>, Any?>): Collection<T>

    fun <T: Any> getById(kClass: KClass<T>, id: Any): T?

    fun <T: Any> save(kClass: KClass<T>, obj: T)

    fun <T: Any> saveAll(kClass: KClass<T>, obj: Collection<T>)

}