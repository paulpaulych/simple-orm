package simpleorm.core.schema

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule


class SchemaDeserializer{

    fun load(string: String): EntityDescriptorList {
        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule())
        return mapper.readValue(string, EntityDescriptorList::class.java)
    }

}
