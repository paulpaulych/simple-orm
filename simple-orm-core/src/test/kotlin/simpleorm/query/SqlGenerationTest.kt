package simpleorm.query

import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import paulpaulych.utils.ResourceLoader
import simpleorm.core.sql.condition.EqualsCondition
import simpleorm.core.sql.FilteringQuery
import simpleorm.core.sql.Query

class SqlGenerationTest: FunSpec(){

    init {

        test("Query test"){
            println(Query(
                    "example",
                    listOf("long_value", "string_value")
            ))
            println(ResourceLoader.loadText("ex_query.sql"))
            Query(
                    "example",
                    listOf("long_value", "string_value")
            ).toString() shouldBe ResourceLoader.loadText("ex_query.sql")
        }

        test("FilteringQuery test"){
            FilteringQuery(
                Query(
                    "example",
                    listOf("long_value", "string_value")
                ),
                listOf(
                        EqualsCondition("long_value", 1),
                        EqualsCondition("string_value", "hello")
                )).toString() shouldBe ResourceLoader.loadText("ex_filtering_query.sql")
        }

        test("InsertStatement test"){
            InsertStatement(
                    "example",
                    mapOf(
                        "long_value" to 3,
                        "string_value" to "hello"
                    )
            ).toString() shouldBe ResourceLoader.loadText("ex_insert_statement.sql")
        }

    }

}