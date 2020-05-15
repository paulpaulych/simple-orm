package simpleorm.core.filter

import kotlin.reflect.KProperty1

interface FetchFilter

interface ParameterizableFetchFilter: FetchFilter{
    val value: Any
}

interface KPropertyFilter: FetchFilter{
    val kProperty: KProperty1<*, *>
}

