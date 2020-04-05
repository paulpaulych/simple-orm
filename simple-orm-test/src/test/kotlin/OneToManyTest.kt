import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
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
import simpleorm.core.schema.yaml.ast.YamlSchemaCreator
import simpleorm.core.sql.SimpleQueryGenerator
import simpleorm.test.manytoone.Owner
import simpleorm.test.manytoone.Product
import java.sql.ResultSet

class OneToManyTest: FunSpec() {

    init {

        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = "jdbc:h2:mem:test"
        hikariConfig.driverClassName = "org.h2.Driver"
        hikariConfig.username = "sa"

        val ormSchema = YamlSchemaCreator(ResourceLoader.loadText("product-owner-schema.yml")).create()

        val jdbc = JdbcTemplate(
                SingleOperationConnectionHolder(
                        HikariDataSource(hikariConfig)
                )
        )

        jdbc.execute("drop table product if exists")
        jdbc.execute("create table product(id bigint primary key, name text, owner_id bigint)")

        jdbc.execute("drop table owner if exists")
        jdbc.execute("create table owner(id bigint primary key, name text)")

        jdbc.execute("insert into owner values(1, 'OWNER_1')")
        jdbc.execute("insert into owner values(2, 'OWNER_2')")

        jdbc.execute("insert into product values(1, 'PRODUCT_1', 1)")
        jdbc.execute("insert into product values(2, 'PRODUCT_2', 1)")

        jdbc.execute("create sequence simpleorm start with 3")

        test("sequence"){
            val next = jdbc.queryForObject("select nextval('simpleorm')"){
                val list = mutableListOf<Long>()
                while(it.next()){
                    list.add(it.getLong(1))
                }
                list
            }
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
                        Product::class to repoProxyGenerator.createRepoProxy(Product::class),
                        Owner::class to repoProxyGenerator.createRepoProxy(Owner::class)
                )
        )

        test("findById"){
            val owner1 = Owner::class.findById(1L)
                    ?: error("not found")

            owner1 shouldBe Owner(1, "OWNER_1",
                    listOf(
                            Product(1, "PRODUCT_1", 1),
                            Product(2, "PRODUCT_2", 1)
                    )
            )

            val owner2 = Owner::class.findById(2L)

            owner2 shouldBe Owner(2, "OWNER_2")
        }

        test("findAll"){

            Owner::class.findAll() shouldBe listOf(
                    Owner(1, "OWNER_1",
                            listOf(
                                    Product(1, "PRODUCT_1", 1),
                                    Product(2, "PRODUCT_2", 1)
                            )
                    ),
                    Owner(2, "OWNER_2")
            )

        }

        test("save new"){
            val owner = Owner(null,"OWNER_3", listOf())
            save(owner) shouldBe Owner(3, "OWNER_3", listOf())

            val product = Product(null, "PRODUCT_3", 4)

            save(product) shouldBe Product(3, "PRODUCT_3", 4)

            Owner::class.findById(3L) shouldBe Owner(3, "OWNER_3", listOf(
                    Product(3, "PRODUCT_3", 3)
            ))


        }

        test("change owner"){

            val oldOwner = Owner::class.findById(3L)
                    ?: error("not found")

            val newOwner = Owner(
                    null,
                    "new owner",
                    listOf()
            )

            save(newOwner).id?.let {id->
                oldOwner.products.map {it.copy(ownerId = id)}.forEach{ save(it) }
            }
            Owner(
                    4,
                    "new owner",
                    listOf(Product(3, "PRODUCT_3", 4))
            )
        }




    }

}