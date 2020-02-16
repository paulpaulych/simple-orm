import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotlintest.TestCaseOrder
import io.kotlintest.matchers.throwable.shouldHaveMessage
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import paulpaulych.utils.ResourceLoader
import simpleorm.*
import simpleorm.core.jdbc.JdbcTemplate
import simpleorm.core.schema.OrmSchemaDescriptor
import simpleorm.core.schema.SchemaParser
import simpleorm.test.ExampleEntity

class OrmTemplateTest : FunSpec(){

    override fun testCaseOrder(): TestCaseOrder? = TestCaseOrder.Sequential

    init {

        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = "jdbc:h2:mem:test"
        hikariConfig.driverClassName = "org.h2.Driver"
        hikariConfig.username = "sa"
        val jdbc = JdbcTemplate(
                HikariDataSource(hikariConfig)
        )
        val ormSchema = OrmSchemaDescriptor(SchemaParser(ResourceLoader.loadText("test-schema.yml")))

        val ormTemplate = OrmTemplate(ormSchema, jdbc)

        jdbc.executeUpdate("create table example(long_value bigint, string_value text)")
        jdbc.executeUpdate("insert into example values(1, 'hello')")
        jdbc.executeUpdate("insert into example values(2, 'halo')")

        test("notInitializedOrmContext"){
            try{
                ExampleEntity::class.getAll()
            }catch (e: Throwable){
                e shouldHaveMessage "orm context is not yet initialized"
            }
        }

        OrmContextProvider.ormContext = OrmContext(ormTemplate)

        test("getAll"){
            val exampleList = ExampleEntity::class.getAll()
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

        test("getById"){
            val example = ExampleEntity::class.getById(1)
            example shouldBe ExampleEntity(1, "hello")
        }

        test("save and get"){
            val id = 3L
            val exampleEntity = ExampleEntity(id, "third")
            save(exampleEntity)
            ExampleEntity::class.getById(id) shouldBe exampleEntity
        }

    }


}