package simpleorm.core.proxy.resulsetextractor

import net.sf.cglib.proxy.*
import simpleorm.core.jdbc.ResultSetExtractor
import simpleorm.core.jdbc.get
import simpleorm.core.schema.property.PlainProperty
import java.lang.reflect.Method
import java.sql.ResultSet
import kotlin.reflect.KClass

class CglibRseProxyGenerator<T: Any>(
        private val propertyDescriptor: PlainProperty<T>
){

    fun create(): ResultSetExtractor<Any> {
        val enhancer = Enhancer()
        enhancer.setInterfaces(arrayOf(ResultSetExtractor::class.java))
        enhancer.setCallback(object : MethodInterceptor{

            override fun intercept(obj: Any, method: Method, args: Array<out Any>, proxy: MethodProxy): Any {
                @Suppress("UNCHECKED_CAST")
                val kClass = propertyDescriptor.kProperty.returnType.classifier as KClass<T>
                if(method.name == "mappedClass"){
                    return kClass
                }
                val resultSet = args.firstOrNull()?.let { it as ResultSet }
                        ?: error("resultSet is null")
                val resultList = mutableListOf<T?>()
                while(resultSet.next()){
                    resultList.add(resultSet.get(propertyDescriptor.column, kClass))
                }
                return resultList
            }

        })
        @Suppress("UNCHECKED_CAST")
        return enhancer.create() as ResultSetExtractor<Any>
    }

}