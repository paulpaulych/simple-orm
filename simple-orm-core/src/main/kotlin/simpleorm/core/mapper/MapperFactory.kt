package simpleorm.core.mapper

import simpleorm.core.schema.OrmSchema
import kotlin.reflect.KClass

class MapperFactory(
        private val schema: OrmSchema
){
    fun <P: Any> byDescriptorMapper(kClass: KClass<P>): BeanRawMapper<P>{
        return ByDescriptorBearRowMapper(kClass, schema)
    }
}