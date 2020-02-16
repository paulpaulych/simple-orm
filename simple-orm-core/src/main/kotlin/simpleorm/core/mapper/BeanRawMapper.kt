package simpleorm.core.mapper

import java.sql.ResultSet

interface BeanRawMapper<T>{

    fun convert(resultSet: ResultSet): List<T>

    fun convert(map: Map<String, Any>): T

}

