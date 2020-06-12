package simpleorm.core.jdbc

import java.sql.Connection

interface ConnectionHolder{

    fun <T: Any> doInConnection(func: (Connection)->T): T

}

