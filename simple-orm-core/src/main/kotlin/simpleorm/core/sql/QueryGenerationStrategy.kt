package simpleorm.core.sql

import simpleorm.core.pagination.Pageable
import simpleorm.core.pagination.Sort
import simpleorm.core.sql.condition.Condition

interface QueryGenerationStrategy{
    fun select(table: String, columns: List<String>, conditions: List<Condition> = listOf()): String
    fun insert(table: String, columns: List<String>): String
    fun update(table: String, columns: List<String>, conditions: List<Condition> = listOf()): String
    fun delete(table: String, conditions: List<Condition>): String
    fun pageableSelect(table: String, columns: List<String>, conditions: List<Condition>, sorts: Map<String, Sort.Order>): String
}