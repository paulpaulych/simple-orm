package simpleorm.core.query.filter

data class InFilter(
        val column: String,
        val values: List<Any>
) {
    override fun toString(): String {
        val valueList = values.fold("") { acc, s ->
            acc + "${if (acc == "") "" else ", "}${if(s is Number) s else "'$s'"}"
        }
        return "$column in values($valueList)"
    }
}