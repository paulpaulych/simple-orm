package simpleorm.core.query.filter

//TODO: preparedStatement?

data class EqualsFilter(
        val column: String,
        val value: Any
): Filter {
    override fun toString(): String {
        return "$column = ${ if(value is Number) value else "'$value'"}"
    }
}

