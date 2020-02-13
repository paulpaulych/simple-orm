//package simpleorm.core.query
//
//import simpleorm.SqlFragment
//
//data class SelectQuery(
//    val table: String,
//    val attributes: List<String>,
//    val filters: List<Filter>
//): SqlFragment {
//
//    override fun toString(): String {
//        val attributes = attributes.fold("") { acc, s ->
//            """$acc${if (acc == "") "" else ","}
//    $s
//            """.trimIndent()
//        }
//        val filters = filters.fold("") { acc, filter ->
//            """$acc
//    ${if (acc == "") "" else "and"} ${filter.toString()}
//            """.trimIndent()
//        }
//        val selectFrom =
//            """select
//    $attributes
//from $table
//        """.trimIndent()
//
//        if(filters.isEmpty()){
//            return selectFrom
//        }
//        return """$selectFrom
//where $filters""".trimIndent()
//    }
//}