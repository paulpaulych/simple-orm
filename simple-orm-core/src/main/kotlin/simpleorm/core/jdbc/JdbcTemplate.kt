package simpleorm.core.jdbc

import paulpaulych.utils.LoggerDelegate
import java.sql.Connection
import java.sql.ResultSet
import kotlin.reflect.KClass

class JdbcTemplate(
    private val connectionHolder: ConnectionHolder
): JdbcOperations {

    override val log by LoggerDelegate()

    override fun <T : Any> doInConnection(callback: (Connection) -> T): T {
        return connectionHolder.doInConnection(callback)
    }

}