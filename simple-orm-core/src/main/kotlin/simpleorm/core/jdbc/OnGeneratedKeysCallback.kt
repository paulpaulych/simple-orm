package simpleorm.core.jdbc

import java.sql.ResultSet


interface OnGeneratedKeysCallback {

    fun doOnGeneratedKeys(keys: ResultSet)

}

