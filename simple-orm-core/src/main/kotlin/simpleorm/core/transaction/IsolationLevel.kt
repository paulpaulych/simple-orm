package simpleorm.core.transaction

import java.sql.Connection

enum class IsolationLevel{
    NONE,
    READ_UNCOMMITTED,
    READ_COMMITTED,
    REPEATABLE_READ,
    SERIALIZABLE
}

fun IsolationLevel.toJavaSqlConst(): Int{
    return when(this){
        IsolationLevel.NONE -> Connection.TRANSACTION_NONE
        IsolationLevel.READ_UNCOMMITTED -> Connection.TRANSACTION_READ_UNCOMMITTED
        IsolationLevel.READ_COMMITTED -> Connection.TRANSACTION_READ_COMMITTED
        IsolationLevel.REPEATABLE_READ -> Connection.TRANSACTION_REPEATABLE_READ
        IsolationLevel.SERIALIZABLE -> Connection.TRANSACTION_SERIALIZABLE
    }
}
