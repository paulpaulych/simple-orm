package simpleorm.core.filter

import simpleorm.core.schema.OrmSchema
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class IsNullFilter(
        override val kProperty: KProperty1<*, *>
): KPropertyFilter

class IsNullFilterResolver(
        ormSchema: OrmSchema
): KPropertyFilterResolver(ormSchema){

    override fun toSql(fetchedType: KClass<*>, filter: FetchFilter): String {
        filter as IsNullFilter
        val column = getColumn(fetchedType, filter.kProperty)
        return "$column is NULL"
    }

}
