import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import paulpaulych.utils.LoggerDelegate
import paulpaulych.utils.ResourceLoader
import simpleorm.core.*
import simpleorm.core.delegate.JdbcDelegateCreator
import simpleorm.core.filter.HashMapFilterResolverRepo
import simpleorm.core.filter.LikeFilter
import simpleorm.core.jdbc.JdbcTemplate
import simpleorm.core.jdbc.SingleOperationConnectionHolder
import simpleorm.core.pagination.PageRequest
import simpleorm.core.pagination.Sort
import simpleorm.core.proxy.CglibDelegateProxyGenerator
import simpleorm.core.proxy.repository.CglibRepoProxyGenerator
import simpleorm.core.schema.naming.SnakeCaseNamingStrategy
import simpleorm.core.schema.yaml.ast.YamlSchemaCreator
import simpleorm.core.sql.SimpleQueryGenerator
import simpleorm.test.Example

class PaginationAndSortTest: FunSpec() {

    private val log by LoggerDelegate()

    init{

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

        jdbc.execute("drop table example if exists")
        jdbc.execute("create table example(long_value bigint auto_increment, string_value text)")
        jdbc.execute("insert into example(string_value) values('first')")
        jdbc.execute("insert into example(string_value) values('second')")
        jdbc.execute("insert into example(string_value) values('third')")
        jdbc.execute("insert into example(string_value) values('fourth')")

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
                        Example::class to repoProxyGenerator.createRepoProxy(Example::class)
                ),
                jdbc,
                CachingDefaultRepoFactory(
                        jdbc,
                        queryGenerationStrategy,
                        filterResolverRepo,
                        ormSchema.namingStrategy
                )
        )

        test("findAll 1 element pages"){
            val pr = PageRequest(0, 1, listOf(Sort(Example::longValue)))
            val firstPage = Example::class.findAll(pr)
            firstPage.values shouldBe listOf(
                    Example(1, "first")
            )
            val pr2 = pr.next
            val secondPage = Example::class.findAll(pr2)
            secondPage.values shouldBe listOf(
                    Example(2, "second")
            )
            val prevPageReq = pr2.prev
                    ?: error("no prev page found")
            val prev = Example::class.findAll(prevPageReq)
            prev.values shouldBe listOf(
                    Example(1, "first")
            )

        }

        test("desc sort by id"){
            val pr = PageRequest(0, 1, listOf(Sort(Example::longValue, Sort.Order.DESC)))
            val firstPage = Example::class.findAll(pr)
            firstPage.values shouldBe listOf(
                    Example(4, "fourth")
            )
            val pr2 = pr.next
            val secondPage = Example::class.findAll(pr2)
            secondPage.values shouldBe listOf(
                    Example(3, "third")
            )
        }

        test("findAll 3 element pages"){
            val requiredFirstPage = listOf(
                    Example(1, "first"),
                    Example(2, "second"),
                    Example(3, "third")
            )
            val pr = PageRequest(0, 3, listOf(Sort(Example::longValue)))
            val firstPage = Example::class.findAll(pr)
            firstPage.values shouldBe requiredFirstPage
            val pr2 = pr.next
            val secondPage = Example::class.findAll(pr2)
            secondPage.values shouldBe listOf(
                    Example(4, "fourth")
            )
            val prevPageReq = pr2.prev
                    ?: error("no prev page found")
            val prev = Example::class.findAll(prevPageReq)
            prev.values shouldBe requiredFirstPage
        }

        test("sort by non-id column"){
            val pr = PageRequest(0, 1, listOf(Sort(Example::stringValue, Sort.Order.DESC)))
            val firstPage = Example::class.findAll(pr)
            firstPage.values shouldBe listOf(
                    Example(3, "third")
            )
            val pr2 = pr.next
            val secondPage = Example::class.findAll(pr2)
            secondPage.values shouldBe listOf(
                    Example(2, "second")
            )
        }

        test("findBy paging"){
            val pageRequest = PageRequest(0, 1, listOf(Sort(Example::longValue, Sort.Order.DESC)))
            val firstPage = Example::class.findBy(
                    listOf(LikeFilter(Example::stringValue, "%i%")),
                    pageRequest)
            firstPage.values shouldBe listOf(
                    Example(3, "third")
            )
            val secondPage = Example::class.findBy(
                    listOf(LikeFilter(Example::stringValue, "%i%")),
                    pageRequest.next)
            secondPage.values shouldBe listOf(
                    Example(1, "first")
            )
        }
    }
}