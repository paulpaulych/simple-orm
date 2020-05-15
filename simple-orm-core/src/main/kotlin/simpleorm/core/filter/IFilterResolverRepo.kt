package simpleorm.core.filter

import kotlin.reflect.KClass

interface IFilterResolverRepo{
    fun getResolver(filterType: KClass<*>): FilterResolver
}