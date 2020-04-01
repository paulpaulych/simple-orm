import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import kotlinx.coroutines.delay
import paulpaulych.utils.ResourceLoader
import simpleorm.core.RepoRegistry
import simpleorm.core.RepoRegistryProvider
import simpleorm.core.delegate.JdbcDelegateCreator
import simpleorm.core.findById
import simpleorm.core.jdbc.JdbcTemplate
import simpleorm.core.jdbc.ResultSetExtractor
import simpleorm.core.proxy.CglibDelegateProxyGenerator
import simpleorm.core.proxy.repository.CglibRepoProxyGenerator
import simpleorm.core.save
import simpleorm.core.schema.yaml.ast.YamlSchemaCreator
import simpleorm.core.sql.SimpleQueryGenerator
import simpleorm.core.transaction.LocalDataSourceTransactionManager
import simpleorm.core.transaction.TransactionManagerHolder
import simpleorm.core.transaction.TxSupportedConnectionHolder
import simpleorm.core.transaction.inTransaction
import simpleorm.test.Person
import java.lang.RuntimeException
import java.sql.ResultSet
import kotlin.concurrent.thread

class TransactionTest : FunSpec(){

    init {

        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = "jdbc:h2:mem:test"
        hikariConfig.driverClassName = "org.h2.Driver"
        hikariConfig.username = "sa"

        val hikariDataSource = HikariDataSource(hikariConfig)
        val ormSchema = YamlSchemaCreator(ResourceLoader.loadText("test-schema.yml")).create()

        TransactionManagerHolder.transactionManager = LocalDataSourceTransactionManager(hikariDataSource)

        val jdbc = JdbcTemplate(
                TxSupportedConnectionHolder(

                )
        )

        jdbc.execute("drop table person if exists")
        jdbc.execute("create table person(id bigint, name text, age integer)")

        jdbc.execute("drop sequence simpleorm if exists")
        jdbc.execute("create sequence simpleorm start with 3")

        test("sequence"){
            val next = jdbc.queryForObject("select next value for simpleorm", object : ResultSetExtractor<Long> {

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
                        Person::class to repoProxyGenerator.createRepoProxy(Person::class)
                )
        )

//        test("without transaction"){
//
//            val changeJob = GlobalScope.launch {
//
//                val saved = save(Person(null, "Person", 18))
//
//                delay(3000L)
//
//                val changed = saved.copy(age = 19)
//                save(changed)
//
//            }
//            delay(3000L)
//            val firstRead = Person::class.findById(4L)
//
//            firstRead shouldBe Person(4, "Person", 18)
//
//            changeJob.join()
//
//            val secondRead = Person::class.findById(4L)
//            secondRead shouldBe Person(4, "Person", 19)
//        }

        test("tx commit"){

            inTransaction {
                save(Person(null, "Person", 21))
            }

            Person::class.findById(4L) shouldBe Person(4, "Person", 21)
        }

        test("tx rollback"){
            try {
                inTransaction {
                    val firstRead = Person::class.findById(4L)

                    firstRead shouldBe Person(4, "Person", 21)

                    save(firstRead!!.copy(age = 23))
                    throw RuntimeException("errorerrorerrorerror")
                }
            }catch (e: Throwable){
                e.message shouldBe "errorerrorerrorerror"
                Person::class.findById(4L) shouldBe Person(4, "Person", 21)
            }
        }

        test("serializable isolation"){

            val lock = Object()

            var read: Person? = null

            val thread = thread(start = true){
                synchronized(lock){
                    inTransaction {
                        if(read == null) {
                            println("waiting")
                            lock.wait()
                        }
                        read = Person::class.findById(5L)
                        read shouldBe Person(5, "Person5", 21)

                    }
                }

            }

            inTransaction {
                val saved = save(Person(null, "Person5", 21))
                saved shouldBe Person(5, "Person5", 21)
            }


            //Todo: нормально синхронизовать потоки
            delay(3000L)
        }


    }


}



fun threadWithException(block: ()->Unit): Thread{
    val trd = thread(start = false, block = block)
    var e: Throwable
    trd.setUncaughtExceptionHandler { thread, throwable ->
        e = throwable
    }
    trd.start()
    return trd
}