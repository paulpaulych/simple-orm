package simpleorm.core

import simpleorm.core.filter.FetchFilter
import simpleorm.core.jdbc.JdbcOperations
import simpleorm.core.pagination.Page
import simpleorm.core.pagination.Pageable
import kotlin.reflect.KClass

interface IRepoRegistry{
    fun <T: Any> findRepo(kClass: KClass<T>): ISimpleOrmRepo<T, Any>
}

class RepoRegistry(
        private val map: Map<KClass<*>, ISimpleOrmRepo<*, *>> = mapOf(),
        private val jdbc: JdbcOperations,
        private val defaultRepoFactory: IDefaultRepoFactory
): IRepoRegistry{

    override fun <T : Any> findRepo(kClass: KClass<T>): ISimpleOrmRepo<T, Any> {
        val repo = map[kClass]
                ?:defaultRepo(kClass)
        @Suppress("UNCHECKED_CAST")
        return repo as ISimpleOrmRepo<T, Any>
    }

    private fun <T: Any> defaultRepo(kClass: KClass<T>): ISimpleOrmRepo<*, *>{
        return defaultRepoFactory.create(kClass)
    }
}

object RepoRegistryProvider{
    var repoRegistry: IRepoRegistry? = null
}

inline fun <reified T: Any> KClass<T>.findById(id: Any): T?{
    return findRepo(T::class).findById(id)
}

inline fun <reified T: Any> KClass<T>.findBy(filter: FetchFilter?): List<T>{
    return findRepo(T::class).findBy(filter)
}

inline fun <reified T: Any> KClass<T>.findBy(filter: FetchFilter?, pageable: Pageable): Page<T>{
    return findRepo(T::class).findBy(filter, pageable)
}

inline fun <reified T: Any> KClass<T>.query(sql: String, params: List<Any> = listOf()): List<T>{
    return findRepo(T::class).query(sql, params)
}

inline fun <reified T: Any> KClass<T>.findAll(): List<T>{
    return findRepo(T::class).findAll()
}

inline fun <reified T: Any> KClass<T>.findAll(pageable: Pageable): Page<T> {
    return findRepo(T::class).findAll(pageable)
}

inline fun <reified T: Any> persist(value: T): T{
    return findRepo(T::class).persist(value)
}

inline fun <reified T: Any> batchInsert(objs: List<T>): List<T>{
    return findRepo(T::class).batchInsert(objs)
}

fun <T: Any> persist(kClass: KClass<T>, value: T): T{
    return findRepo(kClass).persist(value)
}

inline fun <reified T: Any, reified ID: Any> KClass<T>.delete(id: ID){
    return findRepo(T::class).delete(id)
}

fun <T: Any> findRepo(kClass: KClass<T>): ISimpleOrmRepo<T, Any> {
    val repoRegistry = RepoRegistryProvider.repoRegistry
            ?: throw RuntimeException("repoRegistry is not initialized")
    return repoRegistry.findRepo(kClass)
}
