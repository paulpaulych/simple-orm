package simpleorm.core.transaction

import java.sql.Connection

class Transaction(
        val definition: TransactionDefinition,
        val connection: Connection
){

    fun commit() {
        connection.commit()
        connection.close()
    }

    fun rollback() {
        connection.rollback()
        connection.close()
    }

    override fun toString(): String {
        return "Transaction(${definition.id})"
    }
}