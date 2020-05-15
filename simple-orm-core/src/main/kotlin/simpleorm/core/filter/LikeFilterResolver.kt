package simpleorm.core.filter

import simpleorm.core.schema.OrmSchema
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class LikeFilter(
        override val kProperty: KProperty1<*, *>,
        override val value: String
): KPropertyFilter, ParameterizableFetchFilter


class LikeFilterResolver(
        ormSchema: OrmSchema
): KPropertyFilterResolver(ormSchema){

    override fun toSql(fetchedType: KClass<*>, filter: FetchFilter): String {
        filter as LikeFilter
        val column = getColumn(fetchedType, filter.kProperty)
        return "$column LIKE ?"
    }

}