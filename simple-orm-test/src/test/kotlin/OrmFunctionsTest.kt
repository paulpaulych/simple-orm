import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotlintest.TestCaseOrder
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import paulpaulych.utils.ResourceLoader
import simpleorm.core.*
import simpleorm.core.delegate.JdbcDelegateCreator
import simpleorm.core.filter.AndFilter
import simpleorm.core.filter.EqFilter
import simpleorm.core.filter.HashMapFilterResolverRepo
import simpleorm.core.filter.LikeFilter
import simpleorm.core.jdbc.JdbcTemplate
import simpleorm.core.jdbc.SingleOperationConnectionHolder
import simpleorm.core.proxy.CglibDelegateProxyGenerator
import simpleorm.core.proxy.repository.CglibRepoProxyGenerator
import simpleorm.core.schema.naming.SnakeCaseNamingStrategy
import simpleorm.core.sql.SimpleQueryGenerator
import simpleorm.core.schema.yaml.ast.YamlSchemaCreator
import simpleorm.test.Example
import simpleorm.test.Person
import simpleorm.test.WithNullable

class OrmFunctionsTest : FunSpec(){

    override fun testCaseOrder(): TestCaseOrder? = TestCaseOrder.Sequential

    init {

        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = "jdbc:h2:mem:test"
        hikariConfig.driverClassName = "org.h2.Driver"
        hikariConfig.username = "sa"

        val ormSchema = YamlSchemaCreator(ResourceLoader.loadText("test-schema.yml"), SnakeCaseNamingStrategy()).create()

        val jdbc = JdbcTemplate(
                SingleOperationConnectionHolder(
                    HikariDataSource(hikariConfig)
                )
        )

        jdbc.execute("drop table with_nullable if exists")
        jdbc.execute("create table with_nullable(id bigint auto_increment, opt text)")

        jdbc.execute("drop table example if exists")
        jdbc.execute("create table example(long_value bigint auto_increment, string_value text)")
        jdbc.execute("insert into example(string_value) values('hello')")
        jdbc.execute("insert into example(string_value) values('halo')")


        jdbc.execute("drop table person if exists")
        jdbc.execute("create table person(id bigint auto_increment, name text, age integer)")

        val queryGenerationStrategy = SimpleQueryGenerator()
        val filterResolverRepo = HashMapFilterResolverRepo(ormSchema)
        val repoProxyGenerator = CglibRepoProxyGenerator(
                ormSchema,
                jdbc,
                SimpleQueryGenerator(),
                CglibDelegateProxyGenerator(
                        ormSchema,
                        JdbcDelegateCreator(
                                jdbc,
                                queryGenerationStrategy
                        )
                ),
               filterResolverRepo
        )

        RepoRegistryProvider.repoRegistry = RepoRegistry(
            mapOf(
                    Example::class to repoProxyGenerator.createRepoProxy(Example::class),
                    Person::class to repoProxyGenerator.createRepoProxy(Person::class),
                    WithNullable::class to repoProxyGenerator.createRepoProxy(WithNullable::class)
            ),
            jdbc,
            CachingDefaultRepoFactory(
                 jdbc,
                 queryGenerationStrategy,
                 filterResolverRepo,
                 ormSchema.namingStrategy
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
            example shouldBe Example(3, "hello")
        }

        test("save existing"){
            val example = save(Example(3, "goodbye"))
            example shouldBe Example(3, "goodbye")
        }

        test("save existing with nullables"){
            val withNullable = save(WithNullable())
            withNullable shouldBe WithNullable(1, null)
        }

        test("multiple properties"){
            val person = save(Person(null, "Karl", 18))
            person shouldBe Person(1, "Karl", 18)
        }

        test("custom query"){
            save(Person(null, "Bob", 29))

            save(Person(null, "Bob2", 31))

            val result = Person::class.query("select id, name from person where age > 30")
            result shouldBe listOf(Person(3, "Bob2", 31))

            val result2 = Person::class.query("select id, name from person where age > ?", listOf(30))
            result2 shouldBe listOf(Person(3, "Bob2", 31))
        }

//        test("not described class query"){
//            jdbc.execute("create table NotDescribed(id bigint auto_increment, name text)")
//            jdbc.execute("insert into NotDescribed(name) values('first')")
//
//            val res = NotDescribed::class.query("select id, name from NotDescribed")
//            res[0] shouldBe NotDescribed(1, "first")
//        }

        test("findBy test"){
            val examples = Example::class.findBy(EqFilter(Example::stringValue, "goodbye"))
            examples.first() shouldBe Example(3, "goodbye")
        }

        test("findBy many filters"){
            val persons1 = Person::class.findBy(AndFilter(
                    EqFilter(Person::age, 29),
                    LikeFilter(Person::name, "%2")
            ))

            persons1 shouldBe listOf()

            val persons2 = Person::class.findBy(AndFilter(
                    EqFilter(Person::age, 31),
                    LikeFilter(Person::name, "%b2")
            ))

            persons2.first() shouldBe Person(3, "Bob2", 31)
        }

    }

    data class NotDescribed(
         val id: Long? = null,
         val name: String
    )

}