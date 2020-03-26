package simpleorm.core.jdbc

import java.sql.Connection

interface ConnectionCallBack<T: Any>{
    fun <T> doInConnection(conn: Connection): T
}