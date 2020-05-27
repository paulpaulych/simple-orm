package simpleorm.core.schema.naming

class SnakeCaseNamingStrategy: INamingStrategy {
    override fun toTableName(s: String): String {
        return toSnakeCase(s)
    }
    override fun toColumnName(s: String): String {
        return toSnakeCase(s)
    }
}

