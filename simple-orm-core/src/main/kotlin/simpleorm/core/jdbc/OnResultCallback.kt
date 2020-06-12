package simpleorm.core.jdbc

import java.sql.ResultSet

interface OnResultCallback{
    fun doOnResultSet(resultSet: ResultSet)
}