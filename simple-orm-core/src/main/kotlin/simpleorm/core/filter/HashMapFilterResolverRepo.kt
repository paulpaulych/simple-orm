package simpleorm.core.filter

import simpleorm.core.schema.OrmSchema
import kotlin.reflect.KClass

class HashMapFilterResolverRepo(
        ormSchema: OrmSchema,
        resolvers: Map<KClass<*>, FilterResolver>? = null
): IFilterResolverRepo {

    private val resolversMap = mutableMapOf<KClass<*>, FilterResolver>(
            EqFilter::class to EqFilterResolver(ormSchema),
            LikeFilter::class to LikeFilterResolver(ormSchema),
            NonNullFilter::class to NonNullFilterResolver(ormSchema),
            IsNullFilter::class to IsNullFilterResolver(ormSchema),
            AndFilter::class to AndFilterResolver(),
            OrFilter::class to OrFilterResolver(),
            NotEqFilter::class to NotEqFilterResolver(ormSchema),
            LessFilter::class to LessFilterResolver(ormSchema),
            GreaterFilter::class to GreaterFilterResolver(ormSchema),
            LessEqFilter::class to LessEqFilterResolver(ormSchema),
            GreaterEqFilter::class to GreaterEqFilterResolver(ormSchema),
            NotLikeFilter::class to NotLikeFilterResolver(ormSchema)
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