package simpleorm.core.mapper

import simpleorm.core.schema.EntityDescriptorRegistry
import kotlin.reflect.KClass

class MapperFactory(
        private val registry: EntityDescriptorRegistry
){
    fun <P: Any> byDescriptorMapper(kClass: KClass<P>): BeanRawMapper<P>{
        return ByDescriptorBearRowMapper<P>(kClass,registry)
    }
}