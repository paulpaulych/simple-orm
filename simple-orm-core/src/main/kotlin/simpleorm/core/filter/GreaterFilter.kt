package simpleorm.core.filter

import simpleorm.core.schema.OrmSchema
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class GreaterFilter(
        kProperty: KProperty1<*, *>,
        param: Any
): KPropertyFilter(kProperty, listOf(param))


class GreaterFilterResolver(
        ormSchema: OrmSchema
): KPropertyFilterResolver(ormSchema){

    override fun toSql(fetchedType: KClass<*>, filter: FetchFilter, filterResolverRepo: IFilterResolverRepo): String {
        filter as GreaterFilter
        val column = getColumn(fetchedType, filter.kProperty)
        return "$column > ?"
    }

}
