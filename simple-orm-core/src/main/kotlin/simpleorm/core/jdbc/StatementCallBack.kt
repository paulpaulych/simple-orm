package simpleorm.core.jdbc

import java.sql.Statement

interface StatementCallBack<T: Any>{
    fun <T> doInStatement(statement: Statement): T
}