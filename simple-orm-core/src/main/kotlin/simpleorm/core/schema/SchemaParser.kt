package simpleorm.core.schema

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.Reader


class SchemaParser(
        private val rawText: String
){

    constructor(reader: Reader):this(reader.readText())

    fun parse(): EntityDescriptorList {
        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule())
        return mapper.readValue(rawText, EntityDescriptorList::class.java)
    }

}
