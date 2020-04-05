package simpleorm.core.sql

import simpleorm.core.sql.condition.Condition

class SimpleQueryGenerator: QueryGenerationStrategy {

    override fun select(table: String, columns: List<String>, conditions: List<Condition>): String {
        val query = Query(table, columns)
        return FilteringQuery(query, conditions).toString()
    }

    override fun insert(table: String, columns: List<String>): String {
        val keyList = columns.fold("") { acc, s ->
            acc + "${if (acc == "") "" else ", "}$s"
        }

        val valuePlaceholders = Array(columns.size){"?"}.joinToString(", ")

        return """insert into $table ($keyList)
values ($valuePlaceholders)""".trimIndent()
    }

    override fun update(table: String, columns: List<String>, conditions: List<Condition>): String {
        return UpdateStatement(
                table,
                columns,
                conditions
        ).toString()
    }

    override fun delete(table: String, conditions: List<Condition>): String {
        return DeleteStatement(
                table,
                conditions
        ).toString()
    }

}