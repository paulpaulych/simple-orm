package simpleorm.core.filter

import simpleorm.core.schema.OrmSchema
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class IsNullKPropertyFilter(
        override val kProperty: KProperty1<*, *>
): KPropertyFilter

class IsNullKPropertyFilterResolver(
        ormSchema: OrmSchema
): KPropertyFilterResolver(ormSchema){

    override fun toSql(fetchedType: KClass<*>, filter: FetchFilter): String {
        filter as IsNullKPropertyFilter
        val column = getColumn(fetchedType, filter.kProperty)
        return "$column is NULL"
    }

}
