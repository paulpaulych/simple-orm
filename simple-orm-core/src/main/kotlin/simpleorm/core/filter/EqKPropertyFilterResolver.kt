package simpleorm.core.filter

import simpleorm.core.schema.OrmSchema
import simpleorm.core.schema.naming.INamingStrategy
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class EqKPropertyFilter(
        override val kProperty: KProperty1<*, *>,
        override val value: Any
): KPropertyFilter, ParameterizableFetchFilter


class EqKPropertyFilterResolver(
        ormSchema: OrmSchema
): KPropertyFilterResolver(ormSchema){

    override fun toSql(fetchedType: KClass<*>, filter: FetchFilter): String {
        filter as EqKPropertyFilter
        val column = getColumn(fetchedType, filter.kProperty)
        return "$column = ?"
    }

}
