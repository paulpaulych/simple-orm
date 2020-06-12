package simpleorm.core.sql

import simpleorm.core.sql.condition.Condition

data class DeleteStatement(
        val table: String,
        val conditions: List<Condition>
): SqlFragment {

    override fun toString(): String {
        val conditions = conditions.fold("") { acc, cond ->
            """$acc
    ${if (acc == "") "" else "and"} $cond""".trimIndent()
        }

        return """delete from $table 
where ($conditions)""".trimIndent()
    }
}