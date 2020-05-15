package simpleorm.core.pagination

import kotlin.reflect.KProperty1

class Sort(
        val kProperty: KProperty1<*, *>,
        val order: Order = Order.ASC
) {
    enum class Order{ ASC, DESC }
}