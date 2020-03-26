package simpleorm.core.sql

import simpleorm.core.sql.condition.Condition

interface QueryGenerationStrategy{
    fun select(table: String, columns: List<String>, conditions: List<Condition> = listOf()): String
    fun insert(table: String, columns: List<String>, values: List<Any>): String
    fun update(table: String, columns: List<String>, values: List<Any>, conditions: List<Condition> = listOf()): String
    fun delete(table: String, conditions: List<Condition>): String
}