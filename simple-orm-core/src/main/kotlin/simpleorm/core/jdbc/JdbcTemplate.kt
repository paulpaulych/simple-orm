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

    override fun <T : Any> queryForList(prepared: String, params: Map<String, Any>, mapper: BeanRawMapper<T>): List<T> {
        TODO("not implemented")
//        val resultSet = connection.prepareStatement(prepared).executeQuery()
//        return mapper.convert(resultSet)
    }

    override fun queryForResultSet(query: String): ResultSet {
        return connection.createStatement().executeQuery(query)
    }

    override fun queryForResultSet(query: String, params: Map<String, Any>): ResultSet {
        TODO("not implemented")
    }


    override fun <T : Any> queryForObject(query: String, mapper: BeanRawMapper<T>): T? {
        val result = queryForList(query, mapper)
        if(result.size != 1){
            error("excepted result row number: 1, got: ${result.size}")
        }
        return result.first()
    }

    override fun <T : Any> queryForObject(query: String, params: Map<String, Any>, mapper: BeanRawMapper<T>): T? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun executeUpdate(statement: String): Int =
        connection.createStatement().executeUpdate(statement)

    override fun setAutoCommit(flag: Boolean) {
        connection.autoCommit = flag   
    }

    override fun begin() {
        connection.beginRequest()
    }

    override fun commit() {
        connection.commit()
    }

    override fun rollback() {
        connection.rollback()
    }

}