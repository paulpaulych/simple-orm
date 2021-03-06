package integration

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import integration.model.Book
import integration.model.Color
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import paulpaulych.utils.Open
import paulpaulych.utils.ResourceLoader
import simpleorm.core.*
import simpleorm.core.delegate.JdbcDelegateCreator
import simpleorm.core.filter.HashMapFilterResolverRepo
import simpleorm.core.jdbc.JdbcTemplate
import simpleorm.core.jdbc.SingleOperationConnectionHolder
import simpleorm.core.proxy.CglibDelegateProxyGenerator
import simpleorm.core.proxy.repository.CglibRepoProxyGenerator
import simpleorm.core.schema.naming.SnakeCaseNamingStrategy
import simpleorm.core.schema.yaml.ast.YamlSchemaCreator
import simpleorm.core.sql.SimpleQueryGenerator

class ManyToManyTest : FunSpec(){

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

        jdbc.execute("drop table book if exists")
        jdbc.execute("create table book(id bigint primary key auto_increment, name text)")

        jdbc.execute("drop table color if exists")
        jdbc.execute("create table color(id bigint primary key auto_increment, name text)")

        jdbc.execute("drop table book_color if exists")
        jdbc.execute("create table book_color(id bigint primary key auto_increment, book_id bigint, color_id bigint," +
                "foreign key (book_id) references book(id) on delete cascade," +
                "foreign key (color_id) references color(id) on delete cascade)")

        jdbc.execute("insert into book values(null, 'BOOK_1')")
        jdbc.execute("insert into book values(null, 'BOOK_2')")

        jdbc.execute("insert into color values(1, 'COLOR_1')")
        jdbc.execute("insert into color values(2, 'COLOR_2')")

        jdbc.execute("insert into book_color values(1, 1, 2)")

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
                        Book::class to repoProxyGenerator.createRepoProxy(Book::class),
                        Color::class to repoProxyGenerator.createRepoProxy(Color::class)
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
            val book1 = Book::class.findById(1L)
                    ?: error("not found")

            book1 shouldBe Book(1, "BOOK_1",
                    listOf(
                            Color(2, "COLOR_2")
                    )
            )

            val book2 = Book::class.findById(2L)

            book2 shouldBe Book(2, "BOOK_2", listOf())
        }

        test("add one right"){
            val color1 = Color::class.findById(1L)
                    ?: error("not found")
            val book1 = Book::class.findById(1L)
                    ?: error("not found")
            val saved = persist(book1.copy(colors = book1.colors + color1))
            jdbc.queryForList("select color_id from book_color where book_id = 1"){
                val res = mutableListOf<Long>()
                while(it.next()){
                    res.add(it.getLong(1))
                }
                res
            } shouldBe listOf(2L, 1L)
            saved shouldBe Book(1, "BOOK_1",
                listOf(
                        Color(2L, "COLOR_2"),
                        Color(1L, "COLOR_1")
                )
            )
        }

        test("delete one right"){
            val book1 = Book::class.findById(1L)
                    ?: error("not found")
            val saved = persist(book1.copy(colors = book1.colors.filter{ it.id == 1L }))
            saved shouldBe Book(1, "BOOK_1",
                    listOf(
                            Color(1L, "COLOR_1")
                    )
            )
        }

        test("delete color"){
            Color::class.delete(1L)

            Book::class.findById(1L) shouldBe Book(1, "BOOK_1", listOf())
        }

    }


}