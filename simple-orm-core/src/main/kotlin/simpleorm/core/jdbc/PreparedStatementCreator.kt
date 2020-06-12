package simpleorm.core.jdbc

import java.sql.Connection
import java.sql.PreparedStatement

interface PreparedStatementCreator{
    fun create(conn: Connection): PreparedStatement
}