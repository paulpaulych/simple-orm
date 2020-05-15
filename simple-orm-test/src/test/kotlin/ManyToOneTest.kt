import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
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
import simpleorm.core.schema.yaml.ast.YamlSchemaCreator
import simpleorm.core.sql.SimpleQueryGenerator

@Open
data class Article(
        val id: Long? = null,
        val title: String,
        val author: Author
)

@Open
data class Author(
        val id: Long? = null,
        val name: String
)

@Open
data class Woman(
        val id: Long? = null,
        val name: String,
        val husband: Man?
)

@Open
data class Man(
        val id: Long? = null,
        val name: String
)

class ManyToOneTest : FunSpec(){

    init {

        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = "jdbc:h2:mem:test"
        hikariConfig.driverClassName = "org.h2.Driver"
        hikariConfig.username = "sa"

        val ormSchema = YamlSchemaCreator(ResourceLoader.loadText("article-author-schema.yml")).create()

        val jdbc = JdbcTemplate(
                SingleOperationConnectionHolder(
                        HikariDataSource(hikariConfig)
                )
        )

        jdbc.execute("drop table author if exists")
        jdbc.execute("create table author(id bigint primary key auto_increment, name text)")

        jdbc.execute("drop table article if exists")
        jdbc.execute("create table article(id bigint primary key auto_increment, title text, author_id bigint," +
                "foreign key (author_id) references author(id) on delete cascade)")

        jdbc.execute("insert into author values(1, 'AUTHOR_1')")
        jdbc.execute("insert into author values(2, 'AUTHOR_2')")

        jdbc.execute("insert into article values(1, 'ART_1', 2)")

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
                ),
                HashMapFilterResolverRepo(ormSchema)
        )

        RepoRegistryProvider.repoRegistry = RepoRegistry(
                mapOf(
                        Article::class to repoProxyGenerator.createRepoProxy(Article::class),
                        Author::class to repoProxyGenerator.createRepoProxy(Author::class),
                        Woman::class to repoProxyGenerator.createRepoProxy(Woman::class),
                        Man::class to repoProxyGenerator.createRepoProxy(Man::class)
                ),
                jdbc
        )

        test("findById"){
            val article1 = Article::class.findById(1L)
                    ?: error("not found")

            article1 shouldBe Article(1, "ART_1", Author(2, "AUTHOR_2"))
        }




        jdbc.execute("drop table woman if exists")
        jdbc.execute("create table woman(id bigint primary key auto_increment, name text, husband_id bigint)")

        jdbc.execute("drop table man if exists")
        jdbc.execute("create table man(id bigint primary key auto_increment, name text)")

        jdbc.execute("insert into man values(1, 'MAN_1')")
        jdbc.execute("insert into man values(2, 'MAN_2')")

        jdbc.execute("insert into woman values(1, 'WOMAN_1', 2)")
        jdbc.execute("insert into woman values(2, 'WOMAN_2', null)")

        test("nullable manytoone findById"){
            val woman = Woman::class.findById(1L)
                    ?: error("not found")
            woman shouldBe Woman(1, "WOMAN_1", Man(2, "MAN_2"))
        }

        test("null link"){
//            val id = jdbc.queryForObject("select\n" +
//                    "    husband_id\n" +
//                    "from woman\n" +
//                    "where id = '2'"){
//                it.next()
//                listOf<Lon(it.getObject(1))
//            }
//            id shouldBe null
//
            val woman = Woman::class.findById(2L)
                    ?: error("not found")
            println(woman)
            woman shouldBe Woman(2, "WOMAN_2", null)
        }

//        test("add one right"){
//            val color1 = Color::class.findById(1L)
//                    ?: error("not found")
//            val book1 = Book::class.findById(1L)
//                    ?: error("not found")
//            val saved = save(book1.copy(colors = book1.colors + color1))
//            jdbc.queryForList("select color_id from book_color where book_id = 1"){
//                val res = mutableListOf<Long>()
//                while(it.next()){
//                    res.add(it.getLong(1))
//                }
//                res
//            } shouldBe listOf(2L, 1L)
//            saved shouldBe Book(1, "BOOK_1",
//                listOf(
//                        Color(2L, "COLOR_2"),
//                        Color(1L, "COLOR_1")
//                )
//            )
//        }
//
//        test("delete one right"){
//            val book1 = Book::class.findById(1L)
//                    ?: error("not found")
//            val saved = save(book1.copy(colors = book1.colors.filter{ it.id == 1L }))
//            saved shouldBe Book(1, "BOOK_1",
//                    listOf(
//                            Color(1L, "COLOR_1")
//                    )
//            )
//        }
//
//        test("delete color"){
//            Color::class.delete(1L)
//
//            Book::class.findById(1L) shouldBe Book(1, "BOOK_1", listOf())
//        }


    }


}