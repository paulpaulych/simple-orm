package simpleorm.core.transaction

import org.slf4j.LoggerFactory

fun <T: Any> inTransaction(action: () -> T): T{

    //TODO: использовать более подходящий логгер
    val log = LoggerFactory.getLogger(TransactionDefinition::class.java)
    //TODO: добавить генератор уникальных идентификаторов
    val def = TransactionDefinition()

    log.trace("creating transaction by $def in ${Thread.currentThread().name}")

    val transactionManager = TransactionManagerHolder.transactionManager
    val transaction = transactionManager.getTransaction(def)

    TransactionHolder.setCurrentTransaction(transaction)

    val result: T
    try {
        result = action.invoke()
    }catch (e: Throwable){
        transactionManager.rollback(transaction)
        TransactionHolder.clear()
        throw e
    }

    transactionManager.commit(transaction)
    TransactionHolder.clear()
    log.trace("transaction $transaction committed")

    return result
}
