package simpleorm.core.proxy.repository

import simpleorm.core.jdbc.JdbcOperations
import simpleorm.core.jdbc.ResultSetExtractor
import java.sql.ResultSet
import kotlin.reflect.KClass

/**
 * TODO: create interface
 */
class SequenceIdGenerator(
        private val jdbc: JdbcOperations
){

    fun generateId(): Any{
        return jdbc.queryForObject("select next value for simpleorm", object : ResultSetExtractor<Long> {

            override fun extract(resultSet: ResultSet): List<Long> {
                val list = mutableListOf<Long>()
                while(resultSet.next()){
                    list.add(resultSet.getLong(1))
                }
                return list
            }

        })!!
    }

}