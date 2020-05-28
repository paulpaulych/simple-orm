import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import paulpaulych.utils.ResourceLoader
import simpleorm.core.*
import simpleorm.core.delegate.JdbcDelegateCreator
import simpleorm.core.filter.EqFilter
import simpleorm.core.filter.HashMapFilterResolverRepo
import simpleorm.core.jdbc.JdbcTemplate
import simpleorm.core.jdbc.SingleOperationConnectionHolder
import simpleorm.core.pagination.PageRequest
import simpleorm.core.pagination.Sort
import simpleorm.core.proxy.CglibDelegateProxyGenerator
import simpleorm.core.proxy.repository.CglibRepoProxyGenerator
import simpleorm.core.schema.naming.SnakeCaseNamingStrategy
import simpleorm.core.schema.yaml.ast.YamlSchemaCreator
import simpleorm.core.sql.SimpleQueryGenerator
import simpleorm.test.DefaultExample

class DefaultRepoTest: FunSpec() {

    init {
        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = "jdbc:h2:mem:test"
        hikariConfig.driverClassName = "org.h2.Driver"
        hikariConfig.username = "sa"

        val ormSchema = YamlSchemaCreator(ResourceLoader.loadText("book-color-schema.yml"), SnakeCaseNamingStrategy()).create()

        val jdbc = JdbcTemplate(
                SingleOperationConnectionHolder(
                        HikariDataSource(hikariConfig)
                )
        )

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
                mapOf(),
                jdbc,
                CachingDefaultRepoFactory(
                        jdbc,
                        queryGenerationStrategy,
                        filterResolverRepo,
                        ormSchema.namingStrategy
                )
        )

        jdbc.execute("create table default_example(id bigint primary key auto_increment, one_two text)")

        test("insert"){
            save(DefaultExample(null, "first"))
        }

        test("findById"){
            DefaultExample::class.findById(1) shouldBe DefaultExample(1, "first")
        }

        test("findAll"){
            save(DefaultExample(null, "second"))
            DefaultExample::class.findAll() shouldBe listOf(
                    DefaultExample(1, "first"),
                    DefaultExample(2, "second")
            )
        }

        test("findBy"){
            DefaultExample::class.findBy(
                    listOf(EqFilter(DefaultExample::one_two, "second"))
            ) shouldBe listOf(DefaultExample(2, "second"))
        }


        test("findAll pageable"){
            save(DefaultExample(null, "third"))

            val pr1 = PageRequest(0, 1,
                    listOf(Sort(DefaultExample::id, Sort.Order.DESC)))
            DefaultExample::class.findAll(pr1).values shouldBe listOf(
                    DefaultExample(3, "third")
            )
        }

        test("delete"){
            DefaultExample::class.delete(1L)
            DefaultExample::class.findAll() shouldBe listOf(
                    DefaultExample(2, "second"),
                    DefaultExample(3, "third")
            )
        }

    }

}