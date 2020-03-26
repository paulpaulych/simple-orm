package simpleorm.core.jdbc

import paulpaulych.utils.LoggerDelegate
import java.sql.Connection
import java.sql.ResultSet
import kotlin.reflect.KClass

class JdbcTemplate(
    private val connectionHolder: ConnectionHolder
): JdbcOperations {

    private val log by LoggerDelegate()

    override fun <T : Any> execute(connectionCallBack: ConnectionCallBack<T>): T {
        return connectionHolder.doInConnection (connectionCallBack::doInConnection)
    }

    override fun <T : Any> execute(statementCallback: StatementCallBack<T>): T {
        return execute(object : ConnectionCallBack<T>{

            override fun <T> doInConnection(conn: Connection): T {
                val stmt = conn.createStatement()
                return statementCallback.doInStatement(stmt)
            }

        })
    }

    override fun execute(sql: String) {
        logSql(sql)
        connectionHolder.doInConnection { it.createStatement().execute(sql) }
    }

    override fun query(sql: String, onResultCallback: OnResultCallback) {
        logSql(sql)
        connectionHolder.doInConnection {
            val resultSet = it.createStatement().executeQuery(sql)
            onResultCallback.doOnResultSet(resultSet)
        }
    }

    override fun <T : Any> query(sql: String, resultSetExtractor: ResultSetExtractor<T>): List<T> {
        logSql(sql)
        return connectionHolder.doInConnection {
            val resultSet = it.createStatement().executeQuery(sql)
            resultSetExtractor.extract(resultSet)
        }
    }

    override fun <T : Any> query(psc: PreparedStatementCreator, pss: PreparedStatementSetter, rse: ResultSetExtractor<T>): List<T> {
        return connectionHolder.doInConnection {
            val preparedStatement = psc.create(it)
            pss.set(preparedStatement)
            val resultSet = preparedStatement.executeQuery()
            rse.extract(resultSet)
        }
    }

    override fun <T : Any> queryForObject(sql: String, rse: ResultSetExtractor<T>): T? {
        val result = queryForList(sql, rse)
        if(result.size > 1){
            error("unexpected result rows number: ${result.size}, expected: 1")
        }
        return result.firstOrNull()
    }

    override fun <T : Any> queryForObject(psc: PreparedStatementCreator, pss: PreparedStatementSetter, rse: ResultSetExtractor<T>): T? {
        val result = queryForList(psc, pss, rse)
        if(result.size > 1){
            error("unexpected result rows number: ${result.size}, expected: 1")
        }
        return result.firstOrNull()
    }

    override fun <T : Any> queryForList(sql: String, rse: ResultSetExtractor<T>): List<T> {
        return query(sql, rse)
    }

    override fun <T : Any> queryForList(psc: PreparedStatementCreator, pss: PreparedStatementSetter, rse: ResultSetExtractor<T>): List<T> {
        return query(psc, pss, rse)
    }

    override fun queryForResultSet(sql: String): ResultSet {
        logSql(sql)
        return connectionHolder.doInConnection {
            it.createStatement().executeQuery(sql)
        }
    }

    override fun queryForResultSet(psc: PreparedStatementCreator, pss: PreparedStatementSetter): ResultSet {
        return connectionHolder.doInConnection {
            val preparedStatement = psc.create(it)
            pss.set(preparedStatement)
            preparedStatement.executeQuery()
        }
    }

    override fun executeUpdate(sql: String): Int {
        logSql(sql)
        return connectionHolder.doInConnection {
            it.createStatement().executeUpdate(sql)
        }
    }

    override fun executeUpdate(psc: PreparedStatementCreator, pss: PreparedStatementSetter): Int {
        return connectionHolder.doInConnection {
            val preparedStatement = psc.create(it)
            pss.set(preparedStatement)
            preparedStatement.executeUpdate()
        }
    }

    private fun logSql(sql: String){
        log.debug("executing sql: ${sql.replace("\n", " ")}")
    }

}