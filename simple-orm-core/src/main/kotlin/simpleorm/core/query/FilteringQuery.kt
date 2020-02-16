package simpleorm.core.query

data class FilteringQuery(
        val query: Query,
        val filters: List<Filter> = listOf()
): SqlFragment {
    override fun toString(): String {
        val filters = filters.fold("") { acc, filter ->
            """$acc
    ${if (acc == "") "" else "and"} $filter""".trimIndent()
        }
        if(filters.isEmpty()){
            return query.toString()
        }
        return """$query
where $filters""".trimIndent()
    }
}