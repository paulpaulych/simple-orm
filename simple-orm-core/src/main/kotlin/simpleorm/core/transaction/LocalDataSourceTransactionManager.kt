package simpleorm.core.transaction

import java.util.concurrent.ConcurrentHashMap
import javax.sql.DataSource

class LocalDataSourceTransactionManager(
        private val dataSource: DataSource
): TransactionManager {

    private val transactions = ConcurrentHashMap<TransactionDefinition, Transaction>()

    override fun getTransaction(def: TransactionDefinition): Transaction {
        synchronized(this){
            return transactions[def]
                    ?:let {
                        val conn = dataSource.connection
                        conn.autoCommit = false
                        conn.transactionIsolation = def.isolationLevel.toJavaSqlConst()
                        val new = Transaction(def, conn)
                        transactions[def] = new
                        return new
                    }

        }
    }

    override fun commit(tx: Transaction) {
        synchronized(this){
            tx.commit()
            transactions.remove(tx.definition)
        }
    }

    override fun rollback(tx: Transaction) {
        synchronized(this){
            tx.rollback()
            transactions.remove(tx.definition)
        }
    }

}