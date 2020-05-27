package simpleorm.core.proxy

import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import net.sf.cglib.proxy.Enhancer
import paulpaulych.utils.Open
import simpleorm.core.utils.immutableProperties
import simpleorm.core.utils.mutableProperties
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.primaryConstructor

//class DelegatedPropertiesInterceptorTest: FunSpec(){
//
//    private val initialValue = "initial value"
//
//    init {
//        test("hey, Joe!"){
//            val kClass = Class.forName(A::class.java.name).kotlin
//            val interceptor = DelegatedPropertiesInterceptor(
//                    kClass as KClass<Any>,
//                    kClass.mutableProperties().map { it to MutableDelegate(initialValue)}.toMap(),
//                    kClass.immutableProperties().map { it to ImmutableDelegate(initialValue) }.toMap()
//            )
//            val enhancer = Enhancer()
//            enhancer.setSuperclass(A::class.java)
//            enhancer.setCallbackType(DelegatedPropertiesInterceptor::class.java)
//            enhancer.setCallbackFilter { 0 }
//            val proxyClass = enhancer.createClass()
//            println(proxyClass.constructors.map { it.toString() })
////                    arrayOf(
////                            KClass::class.java,
////
////                            ),
//                    arrayOf(String::class.java)
////            )
//            val proxy = (proxyClass.kotlin.primaryConstructor?.call(
//                    kClass,
//                    kClass.mutableProperties().map { it to MutableDelegate(initialValue)}.toMap(),
//                    kClass.immutableProperties().map { it to ImmutableDelegate(initialValue) }.toMap()
//            )?:proxyClass.kotlin.constructors.find { it.parameters.isEmpty() }?.call()
//            ?: error("no constructor found")) as A
//
//            proxy.immutable shouldBe initialValue
//            proxy.mutable shouldBe initialValue
//
//            proxy.mutable = "hello"
//            proxy.mutable shouldBe "hello"
//            proxy.func("h") shouldBe "h"
//
//        }
//    }
//
//}

@Open
private class A(
    val immutable: String = "",
    var mutable: String = ""
){
    fun func(arg: String): String = arg
}

private class MutableDelegate(init: Any): ReadWriteProperty<Any, Any> {

    private var value = init

    override fun getValue(thisRef: Any, property: KProperty<*>): Any {
        return value
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Any) {
        this.value = value
    }

}

private class ImmutableDelegate<T: Any>(init: T): ReadOnlyProperty<Any, T> {

    private var value = init

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return value
    }

}