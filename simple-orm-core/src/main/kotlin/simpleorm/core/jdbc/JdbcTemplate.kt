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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun queryForResultSet(query: String): ResultSet {
        return connection.createStatement().executeQuery(query)
    }

    override fun queryForResultSet(query: String, params: Map<String, String>): ResultSet {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any> queryForObject(query: String, returnType: KClass<T>): T? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any> queryForObject(query: String, params: Map<String, String>, returnType: KClass<T>): T? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun execute(statement: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setAutoCommit() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun begin() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun commit() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun rollback() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}