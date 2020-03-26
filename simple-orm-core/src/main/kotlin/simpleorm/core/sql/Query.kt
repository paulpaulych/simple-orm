package simpleorm.core.sql

data class Query(
    val table: String,
    val columns: List<String>
): SqlFragment {

    override fun toString(): String {
        val attributes = columns.fold("") { acc, s ->
            """$acc${if (acc == "") "" else ","}
    $s""".trimIndent()
        }
        return """select
    $attributes
from $table""".trimIndent()
    }
}
