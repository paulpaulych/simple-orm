import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotlintest.TestCaseOrder
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import simpleorm.core.jdbc.JdbcTemplate
import simpleorm.core.jdbc.ResultSetExtractor
import simpleorm.core.jdbc.SingleOperationConnectionHolder
import simpleorm.test.Example
import java.sql.ResultSet
import kotlin.reflect.KClass

private class ExampleRse: ResultSetExtractor<Example>{

    override fun extract(resultSet: ResultSet): List<Example> {
        val out = mutableListOf<Example>()
        while (resultSet.next()){
            out.add(Example(
                    resultSet.getLong("long_value"),
                    resultSet.getString("string_value")
            ))
        }
        return out
    }

}

class JdbcTemplateTest: FunSpec(){

    override fun testCaseOrder(): TestCaseOrder? = TestCaseOrder.Sequential

    init {
        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = "jdbc:h2:mem:test"
        hikariConfig.driverClassName = "org.h2.Driver"
        hikariConfig.username = "sa"
        val jdbc = JdbcTemplate(
                SingleOperationConnectionHolder(
                        HikariDataSource(hikariConfig)
                )
        )

        test("insert and select"){

            jdbc.execute("create table example(long_value bigint, string_value text)")
            jdbc.executeUpdate("insert into example values(1, 'hello')") shouldBe 1
            jdbc.executeUpdate("insert into example values(2, 'halo')") shouldBe 1

            val exampleList = jdbc.queryForList(
                    "select long_value, string_value from example",
                    ExampleRse())

            exampleList shouldBe listOf(
                    Example(
                            1,
                            "hello"
                    ),
                    Example(
                            2,
                            "halo"
                    ))
        }

    }

}