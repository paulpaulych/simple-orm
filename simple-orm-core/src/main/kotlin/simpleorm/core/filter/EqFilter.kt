package simpleorm.core.filter

import simpleorm.core.schema.OrmSchema
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class EqFilter(
        kProperty: KProperty1<*, *>,
        param: Any
): KPropertyFilter(kProperty, listOf(param))


class EqFilterResolver(
        ormSchema: OrmSchema
): KPropertyFilterResolver(ormSchema){

    override fun toSql(fetchedType: KClass<*>, filter: FetchFilter, filterResolverRepo: IFilterResolverRepo): String {
        filter as EqFilter
        val column = getColumn(fetchedType, filter.kProperty)
        return "$column = ?"
    }

}
