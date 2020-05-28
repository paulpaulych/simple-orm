package simpleorm.core.filter

import kotlin.reflect.KProperty1

abstract class FetchFilter(
        val params: List<Any> = listOf()
)

open class KPropertyFilter(
    val kProperty: KProperty1<*, *>,
    values: List<Any> = listOf()
): FetchFilter(values)

