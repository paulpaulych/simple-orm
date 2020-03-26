package simpleorm.core.sql

data class InsertStatement(
    val table: String,
    val values: Map<String, Any?>
): SqlFragment {

    override fun toString(): String {
        val keyList = values.keys.fold("") { acc, s ->
            acc + "${if (acc == "") "" else ", "}$s"
        }

        val valueList = values.values.fold("") { acc, s ->
            acc + "${if (acc == "") "" else ", "}${if(s is Number) s else "'$s'"}"
        }

        return """insert into $table ($keyList)
values ($valueList)""".trimIndent()
    }
}



