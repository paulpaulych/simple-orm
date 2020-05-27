package simpleorm.core

import kotlin.reflect.KClass

interface IDefaultRepoFactory{
    fun <T: Any> create(kClass: KClass<T>): ISimpleOrmRepo<T, Any>
}