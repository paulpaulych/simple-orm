package simpleorm.core.jdbc

import java.sql.Connection
import javax.sql.DataSource

class SingleOperationConnectionHolder(
        private val dataSource: DataSource
): ConnectionHolder {

    override fun <T : Any> doInConnection(func: (Connection) -> T): T {
        dataSource.connection.use {
            return func.invoke(it)
        }
    }

}
