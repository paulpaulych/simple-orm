package simpleorm.core.query

//TODO: preparedStatement?
data class Filter(
        val column: String,
        val value: Any
): SqlFragment {

    override fun toString(): String =
        "$column = ${ if(value is Number) value else "'$value'"}"
}