package simpleorm.core.jdbc

import simpleorm.core.mapper.BeanRawMapper
import java.sql.ResultSet
import javax.sql.DataSource
import kotlin.reflect.KClass

class JdbcTemplate(
    private val dataSource: DataSource
): JdbcOperations {

    private val connection
        get() = dataSource.connection

    override fun <T : Any> queryForList(query: String, mapper: BeanRawMapper<T>): List<T> {
        val resultSet = connection.createStatement().executeQuery(query)
        return mapper.convert(resultSet)
    }

    override fun <T : Any> queryForList(prepared: String, params: Map<String, String>, returnType: KClass<T>): List<T> {
        error("not implemented") 
    }

    override fun queryForResultSet(query: String): ResultSet {
        return connection.createStatement().executeQuery(query)
    }

    override fun queryForResultSet(query: String, params: Map<String, String>): ResultSet {
        error("not implemented") 
    }

    override fun <T : Any> queryForObject(query: String, returnType: KClass<T>): T? {
        error("not implemented") 
    }

    override fun <T : Any> queryForObject(query: String, params: Map<String, String>, returnType: KClass<T>): T? {
        error("not implemented") 
    }

    override fun executeUpdate(statement: String): Int =
        connection.createStatement().executeUpdate(statement)

    override fun setAutoCommit(flag: Boolean) {
        connection.autoCommit = flag   
    }

    override fun begin() {
        error("not implemented")
    }

    override fun commit() {
        error("not implemented")
    }

    override fun rollback() {
        error("not implemented")
    }

}