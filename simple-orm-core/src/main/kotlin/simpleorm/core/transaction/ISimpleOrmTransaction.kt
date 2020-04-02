package simpleorm.core.transaction

interface ISimpleOrmTransaction {

    val definition: TransactionDefinition

    fun commit()

    fun rollback()

}