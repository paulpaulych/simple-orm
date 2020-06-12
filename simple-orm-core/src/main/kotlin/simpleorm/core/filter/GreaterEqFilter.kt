package simpleorm.core.filter

import simpleorm.core.schema.OrmSchema
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class GreaterEqFilter(
        kProperty: KProperty1<*, *>,
        param: Any
): KPropertyFilter(kProperty, listOf(param))


class GreaterEqFilterResolver(
        ormSchema: OrmSchema
): KPropertyFilterResolver(ormSchema){

    override fun toSql(fetchedType: KClass<*>, filter: FetchFilter, filterResolverRepo: IFilterResolverRepo): String {
        filter as GreaterEqFilter
        val column = getColumn(fetchedType, filter.kProperty)
        return "$column >= ?"
    }

}
