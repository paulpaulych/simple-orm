package simpleorm.core.sql.condition

//TODO: preparedStatement?

data class EqualsCondition(
        val column: String,
        val value: Any? = null
): Condition {
    override fun toString(): String {
        if(value == null) return "$column = ?"
        return "$column = ${ if(value is Number) value else "'$value'"}"
    }
}

data class InCondition(
        val column: String,
        val values: List<Any>
): Condition{
    override fun toString(): String {
        val strings = values.map { "${ if(it is Number) it else "'$it'"}" }
        return "$column in (${strings.joinToString (", " )})"
    }
}