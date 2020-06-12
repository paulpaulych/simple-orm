package simpleorm.core.sql.condition

@Deprecated("use EqFilter")
data class EqualsCondition(
        val column: String,
        val value: Any? = null
): Condition {
    override fun toString(): String {
        if(value == null) return "$column = ?"
        return "$column = ${ if(value is Number) value else "'$value'"}"
    }
}