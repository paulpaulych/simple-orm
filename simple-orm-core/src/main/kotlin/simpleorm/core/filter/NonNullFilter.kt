package simpleorm.core.filter

import simpleorm.core.schema.OrmSchema
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class NonNullFilter(
        override val kProperty: KProperty1<*, *>
): KPropertyFilter

class NonNullFilterResolver(
        ormSchema: OrmSchema
): KPropertyFilterResolver(ormSchema){

    override fun toSql(fetchedType: KClass<*>, filter: FetchFilter): String {
        filter as NonNullFilter
        val column = getColumn(fetchedType, filter.kProperty)
        return "$column is not NULL"
    }

}
