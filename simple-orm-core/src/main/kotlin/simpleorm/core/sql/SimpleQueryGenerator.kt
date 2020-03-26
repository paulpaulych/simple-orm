package simpleorm.core.sql

import simpleorm.core.sql.condition.Condition

class SimpleQueryGenerator: QueryGenerationStrategy {

    override fun select(table: String, columns: List<String>, conditions: List<Condition>): String {
        val query = Query(table, columns)
        return FilteringQuery(query, conditions).toString()
    }

    override fun insert(table: String, columns: List<String>, values: List<Any>): String {
        return InsertStatement(
                table,
                columns.mapIndexed{ index, s ->
                    s to values[index]
                }.toMap()).toString()
    }

    override fun update(table: String, columns: List<String>, values: List<Any>, conditions: List<Condition>): String {
        return UpdateStatement(
                table,
                columns.mapIndexed{ index, s ->
                    s to values[index]
                }.toMap(),
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