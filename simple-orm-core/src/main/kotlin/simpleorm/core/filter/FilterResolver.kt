package simpleorm.core.filter

import kotlin.reflect.KClass

interface FilterResolver{
    fun toSql(fetchedType: KClass<*>, filter: FetchFilter): String
    fun supportedFilterType(): KClass<out FetchFilter>
}