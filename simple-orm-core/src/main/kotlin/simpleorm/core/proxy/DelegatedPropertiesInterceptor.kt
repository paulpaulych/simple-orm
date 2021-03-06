package simpleorm.core.proxy

import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy
import paulpaulych.utils.LoggerDelegate
import java.lang.reflect.Method
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaMethod

/**
 * TODO(add constructor arg validation)
 */
class DelegatedPropertiesInterceptor<T: Any>(
        private val kClass: KClass<T>,
        private val readWriteProperties: Map<KMutableProperty1<T, *>, ReadWriteProperty<T, *>>,
        private val readOnlyProperties: Map<KProperty1<T, *>, ReadOnlyProperty<T, *>>
): MethodInterceptor{

    private val log by LoggerDelegate()

    private val propByOriginalGetters = kClass.declaredMemberProperties
            .filter { isConstructorParameter(it, kClass) }
            .associateBy { it.getter.javaMethod }

    private val propByOriginalSetters = kClass.declaredMemberProperties
            .filter { isConstructorParameter(it, kClass) }
            .filterIsInstance<KMutableProperty1<T, *>>()
            .associateBy { it.setter.javaMethod }
    
    @Suppress("UNCHECKED_CAST")
    override fun intercept(obj: Any, method: Method, args: Array<out Any>, proxy: MethodProxy): Any? {
        propByOriginalGetters[method]?.let{
            log.trace("getter called for $it")
            return if(it is KMutableProperty1<T, *>){
                val delegate = readWriteProperties[it]
                        ?: throwDelegateNotFound()
                delegate.getValue(obj as T, it)
            } else{
                val delegate = readOnlyProperties[it]
                        ?: throwDelegateNotFound()
                delegate.getValue(obj as T, it)
            }
        }
        propByOriginalSetters[method]?.let{
            log.trace("setter called for $it")
            val delegate = readWriteProperties[it]
                    ?: throwDelegateNotFound()
            return  (delegate as ReadWriteProperty<T, Any>).setValue(obj as T, it as KProperty<*>, args[0])
        }
        return proxy.invokeSuper(obj, args)
    }

    private fun throwDelegateNotFound(): Nothing{
        error("delegate not found")
    }

}

fun isConstructorParameter(kProperty1: KProperty1<*, *>, kClass: KClass<*>): Boolean{
    val primaryConstructor =  kClass.primaryConstructor
            ?: error("$kClass has no primary constructor")
    return primaryConstructor.parameters.find{ it.name == kProperty1.name } != null
}