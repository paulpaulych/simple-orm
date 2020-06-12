package simpleorm.core.schema.property

import kotlin.reflect.KProperty1

class IdProperty<T: Any>(
        kProperty: KProperty1<Any, *>,
        column: String
): PlainProperty<T>(
        kProperty as KProperty1<Any, T>,
        column
)