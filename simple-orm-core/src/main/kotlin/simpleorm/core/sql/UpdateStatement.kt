package simpleorm.core.sql

import simpleorm.core.sql.condition.Condition

data class UpdateStatement(
        val table: String,
        val values: List<String>,
        val conditions: List<Condition>
): SqlFragment {

    override fun toString(): String {
        val conditions = conditions.fold("") { acc, cond ->
            """$acc
    ${if (acc == "") "" else "and"} $cond""".trimIndent()
        }
        val values = values.toList().fold("") { acc, k ->
            acc + "${if (acc == "") "" else ", "}$k = ?"
        }
        return """update $table set $values
where $conditions""".trimIndent()
    }

}