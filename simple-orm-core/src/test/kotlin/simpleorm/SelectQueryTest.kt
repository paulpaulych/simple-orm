package simpleorm

import io.kotlintest.matchers.numerics.shouldBeExactly
import io.kotlintest.specs.FunSpec
import tradefirm.simpleorm.query.Filter
import tradefirm.simpleorm.query.SelectQuery

class SelectQueryTest: FunSpec({

    test("Простой селект") {
        val selectQuery = SelectQuery(
            "Product",
            listOf("product_id", "product_name"),
            listOf()
        )
        println(selectQuery.toString())
        1 shouldBeExactly 1
    }

    test("селект с WHERE") {
        val selectQuery = SelectQuery(
            "Product",
            listOf("product_id", "product_name"),
            listOf(Filter("product_id", "1"))
        )
        println(selectQuery.toString())
        1 shouldBeExactly 1
    }
})
