import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotlintest.TestCaseOrder
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import paulpaulych.utils.ResourceLoader
import simpleorm.core.jdbc.JdbcTemplate
import simpleorm.core.mapper.MapperFactory
import simpleorm.core.schema.OrmSchemaDescriptor
import simpleorm.core.schema.SchemaParser
import simpleorm.test.ExampleEntity

class JbdcTemplateTest: FunSpec(){

    override fun testCaseOrder(): TestCaseOrder? = TestCaseOrder.Sequential

    init {
        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = "jdbc:h2:mem:test"
        hikariConfig.driverClassName = "org.h2.Driver"
        hikariConfig.username = "sa"
        val jdbc = JdbcTemplate(
                HikariDataSource(hikariConfig)
        )
        val registry = OrmSchemaDescriptor(SchemaParser(ResourceLoader.loadText("test-schema.yml")))
        val mapperFactory = MapperFactory(registry)

        test("save and get list"){

            jdbc.executeUpdate("create table example(long_value bigint, string_value text)")
            jdbc.executeUpdate("insert into example values(1, 'hello')")
            jdbc.executeUpdate("insert into example values(2, 'halo')")

            val exampleList = jdbc.queryForList("select long_value, string_value from example",
                    mapperFactory.byDescriptorMapper(ExampleEntity::class))

            exampleList shouldBe listOf(
                    ExampleEntity(
                            1,
                            "hello"
                    ),
                    ExampleEntity(
                            2,
                            "halo"
                    ))
        }


    }

}