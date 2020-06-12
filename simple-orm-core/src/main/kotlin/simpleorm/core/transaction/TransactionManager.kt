package simpleorm.core.transaction

interface TransactionManager{

    fun getTransaction(def: TransactionDefinition): Transaction

    fun commit(tx: Transaction)

    fun rollback(tx: Transaction)

}


