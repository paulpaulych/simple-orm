package simpleorm.core

import kotlin.reflect.KClass

interface IRepoRegistry{
    fun <T: Any> findRepo(kClass: KClass<T>): ISimpleOrmRepo<T, Any>
}

class RepoRegistry(
        private val map: Map<KClass<*>, ISimpleOrmRepo<*, *>> = mapOf()
): IRepoRegistry{

    override fun <T : Any> findRepo(kClass: KClass<T>): ISimpleOrmRepo<T, Any> {
        val repo = map[kClass]
                ?: throw RuntimeException("repo not found for ${kClass.qualifiedName}")
        return repo as ISimpleOrmRepo<T, Any>
    }

}

object RepoRegistryProvider{
    var repoRegistry: IRepoRegistry? = null
}

inline fun <reified T: Any> KClass<T>.findById(id: Any): T?{
    return findRepo(T::class).findById(id)
}

inline fun <reified T: Any> save(value: T): T{
    return findRepo(T::class).save(value)
}

inline fun <reified T: Any> KClass<T>.findAll(): List<T>{
    return findRepo(T::class).findAll()
}

inline fun <reified T: Any, reified ID: Any> KClass<T>.delete(id: ID){
    return findRepo(T::class).delete(id)
}

inline fun <reified T: Any> findRepo(kClass: KClass<T>): ISimpleOrmRepo<T, Any> {
    val repoRegistry = RepoRegistryProvider.repoRegistry
            ?: throw RuntimeException("repoRegistry is not initialized")
    return repoRegistry.findRepo(T::class)
}

