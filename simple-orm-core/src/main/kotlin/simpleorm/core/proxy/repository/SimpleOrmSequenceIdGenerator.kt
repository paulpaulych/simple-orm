package simpleorm.core.proxy.repository

import simpleorm.core.jdbc.JdbcOperations
import simpleorm.core.jdbc.ResultSetExtractor
import java.sql.ResultSet
import kotlin.reflect.KClass

/**
 * TODO: create interface
 */
class SimpleOrmSequenceIdGenerator(
        private val jdbc: JdbcOperations
){

    fun generateId(): Any{
        return jdbc.queryForObject("select nextval('simpleorm')") {

            val list = mutableListOf<Long>()
            while(it.next()){
                list.add(it.getLong(1))
            }
            list

        } ?: error("sequence not found")
    }

}