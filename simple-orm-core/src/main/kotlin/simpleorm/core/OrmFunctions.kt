package simpleorm.core

import simpleorm.core.jdbc.JdbcOperations
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface IRepoRegistry{
    fun <T: Any> findRepo(kClass: KClass<T>): ISimpleOrmRepo<T, Any>
}

class RepoRegistry(
        private val map: Map<KClass<*>, ISimpleOrmRepo<*, *>> = mapOf(),
        private val jdbc: JdbcOperations
): IRepoRegistry{

    override fun <T : Any> findRepo(kClass: KClass<T>): ISimpleOrmRepo<T, Any> {
        val repo = map[kClass]
                ?:defaultRepo(kClass)
//                ?: throw RuntimeException("repo not found for ${kClass.qualifiedName}")
        return repo as ISimpleOrmRepo<T, Any>
    }

    private fun <T: Any> defaultRepo(kClass: KClass<T>): ISimpleOrmRepo<*, *>{
        return DefaultRepo<T, Any>(
                jdbc,
                kClass,
                DefaultResultSetExtractor(kClass)
        )
    }
}

object RepoRegistryProvider{
    var repoRegistry: IRepoRegistry? = null
}

inline fun <reified T: Any> KClass<T>.findById(id: Any): T?{
    return findRepo(T::class).findById(id)
}

inline fun <reified T: Any> KClass<T>.query(sql: String, params: List<Any> = listOf()): List<T>{
    return findRepo(T::class).query(sql, params)
}

inline fun <reified T: Any> KClass<T>.findAll(): List<T>{
    return findRepo(T::class).findAll()
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

fun <T: Any, R: Any> KClass<T>.findBy(kClass: KClass<T>, kProperty1: KProperty1<T, R>, value: R): List<T>{
    return findRepo(kClass).findAll().filter { kProperty1.get(it) == value }
}

fun <T: Any> findRepo(kClass: KClass<T>): ISimpleOrmRepo<T, Any> {
    val repoRegistry = RepoRegistryProvider.repoRegistry
            ?: throw RuntimeException("repoRegistry is not initialized")
    return repoRegistry.findRepo(kClass)
}

