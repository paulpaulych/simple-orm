package simpleorm.core.filter

import simpleorm.core.schema.OrmSchema
import kotlin.reflect.KClass

class HashMapFilterResolverRepo(
        ormSchema: OrmSchema,
        resolvers: Map<KClass<FetchFilter>, FilterResolver>? = null
): IFilterResolverRepo {

    private val resolversMap = mutableMapOf<KClass<out FetchFilter>, FilterResolver>(
            EqKPropertyFilter::class to EqKPropertyFilterResolver(ormSchema),
            LikeKPropertyFilter::class to LikeKPropertyFilterResolver(ormSchema),
            NonNullKPropertyFilter::class to NonNullKPropertyFilterResolver(ormSchema),
            IsNullKPropertyFilter::class to IsNullKPropertyFilterResolver(ormSchema)
    )

    init {
        if(resolvers != null){
            resolversMap.putAll(resolvers)
        }
    }

    override fun getResolver(filterType: KClass<*>): FilterResolver {
        return resolversMap[filterType]
                ?: error("filter resolver not found for filter type: $filterType")
    }

}