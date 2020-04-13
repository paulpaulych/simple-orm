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
        jdbc.execute("create table person(id bigint primary key auto_increment, name text, age integer)")

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
                ),
                jdbc
        )

        test("tx commit"){

            inTransaction {
                save(Person(null, "Person", 21))
            }

            Person::class.findById(1L) shouldBe Person(1, "Person", 21)
        }

        test("tx rollback"){
            try {
                inTransaction {
                    val firstRead = Person::class.findById(1L)

                    firstRead shouldBe Person(1, "Person", 21)

                    save(firstRead!!.copy(age = 23))
                    throw RuntimeException("errorerrorerrorerror")
                }
            }catch (e: Throwable){
                e.message shouldBe "errorerrorerrorerror"
                Person::class.findById(1L) shouldBe Person(1, "Person", 21)
            }
        }

        test("serializable isolation 1"){
            val lock = Object()
            var read: Person? = null
            val thread = thread(start = true){
                synchronized(lock){
                    inTransaction {
                        if(read == null) {
                            println("waiting")
                            lock.wait()
                        }
                        read = Person::class.findById(2L)

                    }
                }

            }
            delay(600)
            inTransaction {
                val saved = save(Person(null, "Person5", 21))
                saved shouldBe Person(2, "Person5", 21)

                synchronized(lock){
                    println("notifying")
                    lock.notifyAll()
                }
            }

            //Todo: нормально синхронизовать потоки
            delay(3000L)
            read shouldBe Person(2, "Person5", 21)

        }


        test("serializable isolation 2"){
            val lock = Object()
            var read: Person? = null
            val thread = thread(start = true){
                synchronized(lock){
                    inTransaction {
                        if(read == null) {
                            println("waiting")
                            lock.wait()
                        }
                        println("reading")
                        read = Person::class.findById(3L)
                        lock.notifyAll()
                    }
                }
            }
            delay(600)
            inTransaction {
                println("creating")
                val saved = save(Person(null, "Person6", 21))
                saved shouldBe Person(3, "Person6", 21)

                synchronized(lock){
                    println("notifying")
                    lock.notifyAll()
                    lock.wait()
                }

                println("changing")
                save(Person(3, "Person6", 23))
            }

            //Todo: нормально синхронизовать потоки
            read shouldBe null

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