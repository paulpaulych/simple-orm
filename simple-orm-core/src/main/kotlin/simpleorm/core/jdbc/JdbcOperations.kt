package simpleorm.core.jdbc

import org.slf4j.Logger
import paulpaulych.utils.LoggerDelegate
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import kotlin.reflect.KClass

interface JdbcOperations{

    val log: Logger

    fun <T: Any> doInConnection(callback: (Connection) -> T): T

    fun <T: Any> doInStatement(statementCallback: (Statement) -> T): T {
        return doInConnection {
            val stmt = it.createStatement()
            statementCallback.invoke(stmt)
        }
    }

    fun execute(sql: String) {
        logSql(sql)
        return doInStatement { it.execute(sql) }
    }

    fun query(sql: String, onResultCallback: (ResultSet) -> Unit){
        logSql(sql)
        doInStatement {
            val resultSet = it.executeQuery(sql)
            onResultCallback.invoke(resultSet)
        }
    }

    fun <T : Any> query(sql: String, resultSetExtractor: (ResultSet) -> List<T>): List<T>{
        logSql(sql)
        return doInStatement {
            val resultSet = it.executeQuery(sql)
            resultSetExtractor.invoke(resultSet)
        }
    }

    fun <T: Any> queryForObject(sql: String, resultExtractor: (ResultSet)->List<T>): T? {
        val result = queryForList(sql, resultExtractor)
        if(result.size > 1){
            error("unexpected result rows number: ${result.size}, expected: 1")
        }
        return result.firstOrNull()
    }

    fun <T: Any> queryForList(sql: String, resultExtractor: (ResultSet)->List<T>): List<T> {
        logSql(sql)
        return doInStatement {
            resultExtractor.invoke(it.executeQuery(sql))
        }
    }

    fun update(sql: String): Int{
        logSql(sql)
        return doInStatement { it.executeUpdate(sql) }
    }

    private fun logSql(sql: String){
        log.trace("executing sql: $sql")
    }

}