package simpleorm.core.transaction


object TransactionHolder{

    private val current = ThreadLocal<Transaction?>()

    fun getCurrantTransaction(): Transaction?{
        return current.get()
    }

    fun setCurrentTransaction(tx: Transaction){
        current.set(tx)
    }

    fun clear(){
        current.set(null)
    }

}