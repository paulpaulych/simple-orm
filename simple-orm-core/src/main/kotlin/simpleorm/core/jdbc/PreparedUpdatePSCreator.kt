package simpleorm.core.jdbc

import java.sql.Connection
import java.sql.PreparedStatement

class PreparedUpdatePSCreator(
        val sql: String
): PreparedStatementCreator{

    override fun create(conn: Connection): PreparedStatement =
        conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)

}