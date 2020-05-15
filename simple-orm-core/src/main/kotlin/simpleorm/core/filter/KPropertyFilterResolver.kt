package simpleorm.core.filter

import simpleorm.core.schema.OrmSchema
import simpleorm.core.schema.property.PlainProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

abstract class KPropertyFilterResolver(
    private val ormSchema: OrmSchema
): FilterResolver {

    override fun supportedFilterType(): KClass<out FetchFilter> {
        return KPropertyFilter::class
    }

    protected fun getColumn(fetchedType: KClass<*>, kProperty: KProperty1<*, *>): String{
        val pd = ormSchema.findEntityDescriptor(fetchedType).getPropertyDescriptor(kProperty) as PlainProperty
        return pd.column
    }

}