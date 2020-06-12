package simpleorm.core.sql

import simpleorm.core.pagination.Sort

class PageableQuery(
        val query: FilteringQuery,
        val orderBy: Map<String, Sort.Order>
): SqlFragment {
    override fun toString(): String {
        val limit = "limit ?"
        val offset = "offset ?"
        val orderBy = "order by ${orderBy.toList().map { "${it.first} ${it.second}" }.joinToString(", ")}"
            return """$query
            ${if(!orderBy.isEmpty()) orderBy else ""}
            $limit
            $offset""".trimIndent()
    }
}