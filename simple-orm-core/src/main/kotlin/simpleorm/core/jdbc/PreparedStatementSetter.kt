package simpleorm.core.jdbc

import java.sql.PreparedStatement

interface PreparedStatementSetter{
    fun set(ps: PreparedStatement): PreparedStatement
}