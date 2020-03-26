import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotlintest.TestCaseOrder
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import paulpaulych.utils.ResourceLoader
import simpleorm.core.*
import simpleorm.core.delegate.JdbcDelegateCreator
import simpleorm.core.jdbc.JdbcTemplate
import simpleorm.core.jdbc.ResultSetExtractor
import simpleorm.core.jdbc.SingleOperationConnectionHolder
import simpleorm.core.proxy.CglibDelegateProxyGenerator
import simpleorm.core.proxy.repository.CglibRepoProxyGenerator
import simpleorm.core.sql.SimpleQueryGenerator
import simpleorm.core.schema.yaml.ast.YamlSchemaCreator
import simpleorm.test.Example
import simpleorm.test.Person
import java.sql.ResultSet

class OrmTemplateTest : FunSpec(){

    override fun testCaseOrder(): TestCaseOrder? = TestCaseOrder.Sequential

    init {

        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = "jdbc:h2:mem:test"
        hikariConfig.driverClassName = "org.h2.Driver"
        hikariConfig.username = "sa"

        val ormSchema = YamlSchemaCreator(ResourceLoader.loadText("test-schema.yml")).create()

        val jdbc = JdbcTemplate(
                SingleOperationConnectionHolder(
                    HikariDataSource(hikariConfig)
                )
        )

        jdbc.execute("drop table example if exists")
        jdbc.execute("create table example(long_value bigint, string_value text)")
        jdbc.executeUpdate("insert into example values(1, 'hello')")
        jdbc.executeUpdate("insert into example values(2, 'halo')")


        jdbc.execute("drop table person if exists")
        jdbc.execute("create table person(id bigint, name text, age integer)")

        jdbc.execute("create sequence simpleorm start with 3")

        test("sequence"){
            val next = jdbc.queryForObject("select next value for simpleorm", object : ResultSetExtractor<Long>{

                override fun extract(resultSet: ResultSet): List<Long> {
                    val list = mutableListOf<Long>()
                    while(resultSet.next()){
                        list.add(resultSet.getLong(1))
                    }
                    return list
                }

            })

            next shouldBe 3
        }


        val repoProxyGenerator = CglibRepoProxyGenerator(
                ormSchema,
                jdbc,
                SimpleQueryGenerator(),
                CglibDelegateProxyGenerator(
                        ormSchema,
                        JdbcDelegateCreator(
                                jdbc,
                                SimpleQueryGenerator()
                        )
                )
        )

        RepoRegistryProvider.repoRegistry = RepoRegistry(
            mapOf(
                    Example::class to repoProxyGenerator.createRepoProxy(Example::class),
                    Person::class to repoProxyGenerator.createRepoProxy(Person::class)
            )
        )

        test("findById"){
            val example1 = Example::class.findById(1L)
                    ?: error("not found")

            example1 shouldBe Example(
                    1,
                    "hello"
            )

            val example2 = Example::class.findById(2L)
                    ?: error("")

            example2 shouldBe Example(
                    2,
                    "halo"
            )
        }

        test("findAll"){
            val examples = Example::class.findAll()

            examples shouldBe listOf(
                    Example(
                            1,
                            "hello"
                    ),
                    Example(
                            2,
                            "halo"
                    )
            )
        }

        test("delete"){

            Example::class.delete(1L)

            Example::class.findById(1L) shouldBe null

        }


        test("save new"){
            val example = save(Example(null, "hello"))
            example shouldBe Example(4, "hello")
        }

        test("save existing"){
            val example = save(Example(4, "goodbye"))
            example shouldBe Example(4, "goodbye")
        }

        test("person"){
            val person = save(Person(null, "Karl", 18))
            person shouldBe Person(5, "Karl", 18)


        }

    }

}