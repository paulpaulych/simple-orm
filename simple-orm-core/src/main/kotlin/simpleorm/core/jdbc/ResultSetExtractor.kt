package simpleorm.core.jdbc

import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface ResultSetExtractor<T: Any>{

    fun extract(resultSet: ResultSet): List<T>

}