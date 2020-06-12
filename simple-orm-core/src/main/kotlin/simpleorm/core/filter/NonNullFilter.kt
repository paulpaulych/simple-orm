package simpleorm.core.filter

import simpleorm.core.schema.OrmSchema
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class NonNullFilter(
        kProperty: KProperty1<*, *>
): KPropertyFilter(kProperty)

class NonNullFilterResolver(
        ormSchema: OrmSchema
): KPropertyFilterResolver(ormSchema){

    override fun toSql(fetchedType: KClass<*>, filter: FetchFilter, filterResolverRepo: IFilterResolverRepo): String {
        filter as NonNullFilter
        val column = getColumn(fetchedType, filter.kProperty)
        return "$column is not NULL"
    }

}
