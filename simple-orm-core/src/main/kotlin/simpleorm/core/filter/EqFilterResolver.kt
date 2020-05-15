package simpleorm.core.filter

import simpleorm.core.schema.OrmSchema
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class EqFilter(
        override val kProperty: KProperty1<*, *>,
        override val value: Any
): KPropertyFilter, ParameterizableFetchFilter


class EqFilterResolver(
        ormSchema: OrmSchema
): KPropertyFilterResolver(ormSchema){

    override fun toSql(fetchedType: KClass<*>, filter: FetchFilter): String {
        filter as EqFilter
        val column = getColumn(fetchedType, filter.kProperty)
        return "$column = ?"
    }

}