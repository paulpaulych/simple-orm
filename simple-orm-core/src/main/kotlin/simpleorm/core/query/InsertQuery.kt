//package simpleorm.core.query
//
//import simpleorm.SqlFragment
//
//
//data class InsertQuery(
//    val table: String,
//    val values: Map<String, Any>
//): SqlFragment {
//
//    private val paramKeys
//        get() = values.keys
//
//    override fun toString(): String {
//        val keyList = paramKeys.fold("") { acc, s ->
//            acc + "${if (acc == "") "" else ","} $s"
//        }
//
//        val keyPlaceholders = paramKeys.fold("") { acc, s ->
//            """$acc${if (acc == "") "" else ","}
//    :$s
//            """.trimIndent()
//        }
//
//        return """insert into $table ($keyList)
//    $keyPlaceholders
//       """.trimIndent()
//    }
//
//    val paramValues
//        get() = values.values
//}