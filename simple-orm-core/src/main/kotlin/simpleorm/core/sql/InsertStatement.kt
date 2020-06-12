package simpleorm.core.sql

import paulpaulych.utils.LoggerDelegate

data class InsertStatement(
        val table: String,
        val columns: List<String>,
        val valuesList: List<List<Any?>>
){
    private val log by LoggerDelegate()

    override fun toString(): String {
        val keyList = columns.fold("") { acc, s ->
            acc + "${if (acc == "") "" else ", "}$s"
        }

        val valuesBlock = Array(columns.size){"?"}.joinToString(", ")
        log.info("valuesBlock: $valuesBlock"+ "($valuesBlock)")
        log.info("valuesList size: ${valuesList.size}")
        val valuesBlocks = Array(valuesList.size){"($valuesBlock)" }
                .joinToString(", ${System.lineSeparator()}" )
        log.info("valuesBlocks $valuesBlocks")
        return """insert into $table ($keyList)
values $valuesBlocks""".trimIndent()
    }

}