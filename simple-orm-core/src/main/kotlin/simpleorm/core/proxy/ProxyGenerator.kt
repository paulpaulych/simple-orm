package simpleorm.core.proxy

import kotlin.reflect.KClass

interface ProxyGenerator{
    fun <T: Any> createProxyClass(kClass: KClass<T>, id: Any): T
}

