package simpleorm.core.schema.property

open class PlainProperty(
    val column: String
): PropertyDescriptor

class IdProperty(column: String): PlainProperty(column)