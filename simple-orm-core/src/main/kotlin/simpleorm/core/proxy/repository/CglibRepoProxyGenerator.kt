package simpleorm.core.proxy.repository

import net.sf.cglib.proxy.Enhancer
import simpleorm.core.ISimpleOrmRepo
import simpleorm.core.filter.IFilterResolverRepo
import simpleorm.core.jdbc.JdbcOperations
import simpleorm.core.proxy.ProxyGenerator
import simpleorm.core.schema.OrmSchema
import simpleorm.core.sql.QueryGenerationStrategy
import kotlin.reflect.KClass

class CglibRepoProxyGenerator(
        private val ormSchema: OrmSchema,
        private val jdbc: JdbcOperations,
        private val queryGenerationStrategy: QueryGenerationStrategy,
        private val proxyGenerator: ProxyGenerator,
        private val filterResolverRepo: IFilterResolverRepo
): IRepoProxyGenerator {

    override fun <T : Any> createRepoProxy(kClass: KClass<T>): ISimpleOrmRepo<T, *> {
        val enhancer = Enhancer()
        enhancer.setInterfaces(arrayOf(ISimpleOrmRepo::class.java))
        enhancer.setCallback(RepoMethodInterceptor(
                ormSchema.findEntityDescriptor(kClass),
                jdbc,
                queryGenerationStrategy,
                proxyGenerator,
                filterResolverRepo
        ))
        return enhancer.create() as ISimpleOrmRepo<T, *>
    }

}