package simpleorm

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import simpleorm.core.jdbc.JdbcTemplate
import simpleorm.core.mapper.SimplePropertyRawMapper


fun main(){

    val hikariConfig = HikariConfig()
    hikariConfig.jdbcUrl = "jdbc:postgresql://localhost:5432/tradefirm"
    hikariConfig.username = "postgres"
    hikariConfig.password = "111111"
    hikariConfig.driverClassName = "org.postgresql.Driver"

    val jdbc = JdbcTemplate(HikariDataSource(hikariConfig))
    val resultList =
        jdbc.queryForList("select product_id, product_name from product",
            SimplePropertyRawMapper(Product::class)
        )
    println(resultList)

}

data class Product(
    val product_id: Long,
    val product_name: String
)