package simpleorm.core.jdbc

import simpleorm.core.mapper.BeanRawMapper
import java.sql.ResultSet
import kotlin.reflect.KClass

interface JdbcOperations{


    fun <T: Any> queryForList(query: String, mapper: BeanRawMapper<T>): List<T>
    fun <T: Any> queryForList(prepared: String, params: Map<String, Any>,  mapper: BeanRawMapper<T>): List<T>

    fun queryForResultSet(query: String): ResultSet
    fun queryForResultSet(query: String, params: Map<String, Any>): ResultSet

    fun <T: Any> queryForObject(query: String, mapper: BeanRawMapper<T>): T?
    fun <T: Any> queryForObject(query: String, params: Map<String, Any>, mapper: BeanRawMapper<T>): T?

    fun executeUpdate(statement: String): Int

    fun setAutoCommit(flag: Boolean)

    fun begin()
    fun commit()
    fun rollback()

}