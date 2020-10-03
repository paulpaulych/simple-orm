package simpleorm.core.filter

import simpleorm.core.schema.OrmSchema
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class NotLikeFilter(
        kProperty: KProperty1<*, *>,
        param: String
): KPropertyFilter(kProperty, listOf(param))


class NotLikeFilterResolver(
        ormSchema: OrmSchema
): KPropertyFilterResolver(ormSchema){

    override fun toSql(fetchedType: KClass<*>, filter: FetchFilter, filterResolverRepo: IFilterResolverRepo): String {
        filter as NotLikeFilter
        val column = getColumn(fetchedType, filter.kProperty)
        return "$column NOT LIKE ?"
    }

}