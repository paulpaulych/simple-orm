import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import net.sf.cglib.proxy.Enhancer
import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy
import paulpaulych.utils.LoggerDelegate
import paulpaulych.utils.Open
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlinFunction

class ProxyTest : FunSpec(){

    init {
        test("getter interception"){
            val enhancer = Enhancer()
            enhancer.setSuperclass(Foo::class.java)
            enhancer.setCallback(TestEntityProxy(Foo::class))
            println(Foo::class.java.constructors.map { it.kotlinFunction?.parameters })
            val proxyInstance = enhancer.create(
                    arrayOf(Long::class.java, String::class.java),
                    arrayOf(1L, "FooName")
            ) as Foo

            proxyInstance shouldBe Foo(1L, "FooName")
            proxyInstance.hello("h") shouldBe "h"
        }
    }

}



private class TestEntityProxy(
        private val kClass: KClass<*>
):MethodInterceptor{

    private val log by LoggerDelegate()

    private val properties = kClass.declaredMemberProperties

    private val getters = properties.map { it.getter.javaMethod }

    private val setters = properties.filterIsInstance<KMutableProperty1<*, *>>().map{ it.setter.javaMethod }

    override fun intercept(obj: Any, method: Method, args: Array<out Any>, proxy: MethodProxy): Any {
        if(getters.contains(method)){
            log.trace("getter invoked")
            return proxy.invokeSuper(obj, args)
        }
        if(setters.contains(method)) {
            log.trace("setter invoked")
            proxy.invokeSuper(obj, args)
            return ""
        }
        log.trace("original method invoked..")
        return proxy.invokeSuper(obj, args)
    }
}

@Open
private data class Foo(
        val id: Long,
        val name: String
){
    fun hello(s: String): String{
        return "Hello, $s!"
    }
}
