import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import paulpaulych.utils.LoggerDelegate
import paulpaulych.utils.ResourceLoader
import simpleorm.OrmContext
import simpleorm.OrmContextProvider
import simpleorm.OrmTemplate
import simpleorm.core.jdbc.JdbcTemplate
import simpleorm.core.schema.SchemaParser
import simpleorm.core.schema.toOrmSchema
import simpleorm.getById
import simpleorm.test.manytoone.Owner
import simpleorm.test.manytoone.Product

class ManyToOneTest: FunSpec(){

    private val loggerDelegate by LoggerDelegate()

    init {

        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = "jdbc:h2:mem:test"
        hikariConfig.driverClassName = "org.h2.Driver"
        hikariConfig.username = "sa"
        val jdbc = JdbcTemplate(
                HikariDataSource(hikariConfig)
        )

        val ormSchema = SchemaParser(ResourceLoader.loadText("product-owner-schema.yml")).parse().toOrmSchema()

        val ormTemplate = OrmTemplate(ormSchema, jdbc)

        OrmContextProvider.ormContext = OrmContext(ormTemplate)

        jdbc.executeUpdate("create table product(id bigint, name text, owner_id bigint)")
        jdbc.executeUpdate("insert into product values(1, 'product1', 1)")
        jdbc.executeUpdate("insert into product values(2, 'product2', 1)")

        jdbc.executeUpdate("create table owner(id bigint, name text)")
        jdbc.executeUpdate("insert into owner values(1, 'owner1')")
        jdbc.executeUpdate("insert into owner values(2, 'owner2')")

        test("getById many-to-one"){

            Owner::class.getById(1) shouldBe Owner(
                    id = 1,
                    name = "owner1",
                    products = listOf(
                            Product(
                                    id = 1,
                                    name = "product1",
                                    ownerId = 1
                            ),
                            Product(
                                    id = 2,
                                    name = "product2",
                                    ownerId = 1
                            )
                    )
            )

        }

    }

}