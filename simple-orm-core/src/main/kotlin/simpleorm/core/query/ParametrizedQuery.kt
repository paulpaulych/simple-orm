package simpleorm.core.query

class ParametrizedQuery(
    val table: String,
    val parameters: Map<String, Any>
)