package simpleorm.core.jdbc

import java.sql.ResultSet
import kotlin.reflect.KClass

interface JdbcOperations{

    fun <T: Any> execute(connectionCallBack: ConnectionCallBack<T>): T

    fun <T: Any> execute(statementCallback: StatementCallBack<T>): T

    fun execute(sql: String)

    fun query(sql: String, onResultCallback: OnResultCallback)

    fun <T : Any> query(sql: String, resultSetExtractor: ResultSetExtractor<T>): List<T>

    fun <T: Any> query(psc: PreparedStatementCreator, pss: PreparedStatementSetter, rse: ResultSetExtractor<T>): List<T>

    fun <T: Any> queryForObject(sql: String, rse: ResultSetExtractor<T>): T?

    fun <T: Any> queryForList(sql: String, rse: ResultSetExtractor<T>): List<T>

    fun <T: Any> queryForObject(psc: PreparedStatementCreator, pss: PreparedStatementSetter, rse: ResultSetExtractor<T>): T?

    fun <T: Any> queryForList(psc: PreparedStatementCreator, pss: PreparedStatementSetter, rse: ResultSetExtractor<T>): List<T>

    fun queryForResultSet(sql: String): ResultSet

    fun queryForResultSet(psc: PreparedStatementCreator, pss: PreparedStatementSetter): ResultSet

    fun executeUpdate(sql: String): Int

    fun executeUpdate(psc: PreparedStatementCreator, pss: PreparedStatementSetter): Int

}