package simpleorm.core.filter

import simpleorm.core.schema.OrmSchema
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class LikeFilter(
        kProperty: KProperty1<*, *>,
        param: String
): KPropertyFilter(kProperty, listOf(param))


class LikeFilterResolver(
        ormSchema: OrmSchema
): KPropertyFilterResolver(ormSchema){

    override fun toSql(fetchedType: KClass<*>, filter: FetchFilter, filterResolverRepo: IFilterResolverRepo): String {
        filter as LikeFilter
        val column = getColumn(fetchedType, filter.kProperty)
        return "$column LIKE ?"
    }

}