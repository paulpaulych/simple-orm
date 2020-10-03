package simpleorm.core

import simpleorm.core.filter.IFilterResolverRepo
import simpleorm.core.jdbc.JdbcOperations
import simpleorm.core.schema.naming.INamingStrategy
import simpleorm.core.schema.naming.SnakeCaseNamingStrategy
import simpleorm.core.sql.QueryGenerationStrategy
import kotlin.reflect.KClass

class CachingDefaultRepoFactory(
        private val jdbc: JdbcOperations,
        private val queryGenerationStrategy: QueryGenerationStrategy,
        private val filterResolverRepo: IFilterResolverRepo,
        private val namingStrategy: INamingStrategy
            = SnakeCaseNamingStrategy()
): IDefaultRepoFactory {

    private val repos = mutableMapOf<KClass<*>, ISimpleOrmRepo<*, *>>()

    override fun <T : Any> create(kClass: KClass<T>): ISimpleOrmRepo<T, Any> {
        var repo = repos[kClass]

        if(repo == null){
            repo = DefaultRepo<T, Any>(
                    jdbc,
                    kClass,
                    namingStrategy,
                    queryGenerationStrategy,
                    filterResolverRepo
            )
            repos[kClass] = repo
            return repo
        }

        @Suppress("UNCHECKED_CAST")
        return repo as ISimpleOrmRepo<T, Any>
    }

}