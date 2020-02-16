package simpleorm.core.mapper

import simpleorm.core.schema.OrmSchemaDescriptor
import kotlin.reflect.KClass

class MapperFactory(
        private val schema: OrmSchemaDescriptor
){
    fun <P: Any> byDescriptorMapper(kClass: KClass<P>): BeanRawMapper<P>{
        return ByDescriptorBearRowMapper(kClass, schema)
    }
}