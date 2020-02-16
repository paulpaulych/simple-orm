package simpleorm.core.query

//TODO: preparedStatement?
data class Filter(
        val column: String,
        val value: Any
): SqlFragment {

    override fun toString(): String {
        return "$column = ${ if(value is Number) value else "'$value'"}"
    }

}