package simpleorm.core.transaction

import java.util.concurrent.atomic.AtomicLong

data class TransactionDefinition(
        val id: Long = next(),
        val isolationLevel: IsolationLevel = IsolationLevel.SERIALIZABLE,
        val readOnly: Boolean = false
){
    companion object{
        private var counter = AtomicLong()
        fun next(): Long{
            return counter.incrementAndGet()
        }
    }
}