package simpleorm.core.sql

import simpleorm.core.sql.condition.Condition

data class FilteringQuery(
        val query: Query,
        val conditions: List<Condition> = listOf()
): SqlFragment {
    override fun toString(): String {
        val conditions = conditions.fold("") { acc, cond ->
            """$acc
    ${if (acc == "") "" else "and"} $cond""".trimIndent()
        }
        if(conditions.isEmpty()){
            return query.toString()
        }
        return """$query
where $conditions""".trimIndent()
    }
}


