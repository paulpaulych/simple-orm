package simpleorm.core.schema

interface SchemaCreator{
    fun create(): OrmSchema
}