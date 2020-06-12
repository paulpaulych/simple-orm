package simpleorm.core.proxy.repository

import simpleorm.core.ISimpleOrmRepo
import kotlin.reflect.KClass

interface IRepoProxyGenerator {

    fun <T : Any> createRepoProxy(kClass: KClass<T>): ISimpleOrmRepo<T, *>

}

