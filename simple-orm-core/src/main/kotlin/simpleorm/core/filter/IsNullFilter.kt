package simpleorm.core.filter

import simpleorm.core.schema.OrmSchema
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class IsNullFilter(
        kProperty: KProperty1<*, *>
): KPropertyFilter(kProperty)

class IsNullFilterResolver(
        ormSchema: OrmSchema
): KPropertyFilterResolver(ormSchema){

    override fun toSql(fetchedType: KClass<*>, filter: FetchFilter, filterResolverRepo: IFilterResolverRepo): String {
        filter as IsNullFilter
        val column = getColumn(fetchedType, filter.kProperty)
        return "$column is NULL"
    }

}
