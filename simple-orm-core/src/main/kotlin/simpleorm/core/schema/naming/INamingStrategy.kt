package simpleorm.core.schema.naming

interface INamingStrategy {
    fun toColumnName(s: String): String
    fun toTableName(s: String): String
}

