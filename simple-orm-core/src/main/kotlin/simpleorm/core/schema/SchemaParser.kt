package simpleorm.core.schema

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import simpleorm.core.schema.ast.RawOrmSchema


class SchemaParser(
        private val rawText: String
){

    fun parse(): RawOrmSchema {
        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule())
        return mapper.readValue(rawText, RawOrmSchema::class.java)
    }

}
