package simpleorm.core.sql

import simpleorm.core.sql.condition.Condition

data class UpdateStatement(
        val table: String,
        val values: Map<String, Any?>,
        val conditions: List<Condition>
): SqlFragment {

    override fun toString(): String {
        val conditions = conditions.fold("") { acc, cond ->
            """$acc
    ${if (acc == "") "" else "and"} $cond""".trimIndent()
        }
        val values = values.toList().fold("") { acc, (k, v)  ->
            acc + "${if (acc == "") "" else ", "}$k = ${if(v is Number) v else "'$v'"}"
        }
        return """update $table set $values
where $conditions""".trimIndent()
    }

}