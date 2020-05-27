package simpleorm.core.filter

import simpleorm.core.schema.OrmSchema
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class NonNullKPropertyFilter(
        override val kProperty: KProperty1<*, *>
): KPropertyFilter

class NonNullKPropertyFilterResolver(
        ormSchema: OrmSchema
): KPropertyFilterResolver(ormSchema){

    override fun toSql(fetchedType: KClass<*>, filter: FetchFilter): String {
        filter as NonNullKPropertyFilter
        val column = getColumn(fetchedType, filter.kProperty)
        return "$column is not NULL"
    }

}
