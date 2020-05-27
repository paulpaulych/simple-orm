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

inline fun <reified T: Any> KClass<T>.findBy(filters: List<FetchFilter>): List<T>{
    return findRepo(T::class).findBy(filters)
}

inline fun <reified T: Any> KClass<T>.findBy(filters: List<FetchFilter>, pageable: Pageable): Page<T>{
    return findRepo(T::class).findBy(filters, pageable)
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

inline fun <reified T: Any> save(value: T): T{
    return findRepo(T::class).save(value)
}

fun <T: Any> save(kClass: KClass<T>, value: T): T{
    return findRepo(kClass).save(value)
}

inline fun <reified T: Any, reified ID: Any> KClass<T>.delete(id: ID){
    return findRepo(T::class).delete(id)
}


internal fun <T: Any> KClass<T>.findBy(kClass: KClass<T>, filters: List<FetchFilter>): List<T>{
    return findRepo(kClass).findBy(filters)
}

fun <T: Any> findRepo(kClass: KClass<T>): ISimpleOrmRepo<T, Any> {
    val repoRegistry = RepoRegistryProvider.repoRegistry
            ?: throw RuntimeException("repoRegistry is not initialized")
    return repoRegistry.findRepo(kClass)
}
