package simpleorm.core.transaction

import paulpaulych.utils.LoggerDelegate
import simpleorm.core.jdbc.ConnectionHolder
import java.sql.Connection

class TxSupportedConnectionHolder: ConnectionHolder {

    private val log by LoggerDelegate()

    override fun <T : Any> doInConnection(func: (Connection) -> T): T {
        val transactionManager = TransactionManagerHolder.transactionManager
        synchronized(this) {
            val tx = TransactionHolder.getCurrantTransaction()
            if (tx == null) {
                log.trace("creating single op transaction")
                val newTx = transactionManager.getTransaction(TransactionDefinition())
                val res: T
                try{
                    res = func.invoke(newTx.connection)
                    transactionManager.commit(newTx)
                    log.trace("single op transaction committed")
                }catch(e: Throwable){
                    log.error(e.message)
                    transactionManager.rollback(newTx)
                    throw e
                }
                return res
            }
            log.trace("using existing transaction")
            return func.invoke(tx.connection)
        }

    }

}

//
//class TransactionManager(
//        private val dataSource: DataSource
//): ITransactionManager {
//
//    override var defaultLvl = IsolationLevel.REPEATABLE_READ
//
//    override fun <T : Any> doInTransaction(action: (Connection) -> T, isolationLevel: IsolationLevel) {
//        dataSource.connection.use {
//            it.autoCommit = false
//            it.transactionIsolation = convertLevel(isolationLevel)
//            try{
//                action.invoke(it)
//                it.commit()
//            }catch (e: Throwable) {
//                it.rollback()
//                log.error(e.message)
//                throw e
//            }
//        }
//    }
//
//    private val log by LoggerDelegate()
//
//
//
//}
//
//
////