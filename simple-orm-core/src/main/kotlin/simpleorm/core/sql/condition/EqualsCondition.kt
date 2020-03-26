package simpleorm.core.sql.condition

//TODO: preparedStatement?

data class EqualsCondition(
        val column: String,
        val value: Any
): Condition {
    override fun toString(): String {
        return "$column = ${ if(value is Number) value else "'$value'"}"
    }
}

