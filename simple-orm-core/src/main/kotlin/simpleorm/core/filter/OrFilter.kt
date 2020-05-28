package simpleorm.core.filter

import kotlin.reflect.KClass

class OrFilter(
        val left: FetchFilter,
        val right: FetchFilter
): FetchFilter(left.params + right.params)

class OrFilterResolver : FilterResolver {

    override fun supportedFilterType(): KClass<out FetchFilter> {
        return AndFilter::class
    }

    override fun toSql(fetchedType: KClass<*>, filter: FetchFilter, filterResolverRepo: IFilterResolverRepo): String {
        filter as AndFilter
        val left = filterResolverRepo.getResolver(filter.left::class).toSql(fetchedType, filter.left, filterResolverRepo)
        val right = filterResolverRepo.getResolver(filter.right::class).toSql(fetchedType, filter.right, filterResolverRepo)
        return "($left or $right)"
    }

}