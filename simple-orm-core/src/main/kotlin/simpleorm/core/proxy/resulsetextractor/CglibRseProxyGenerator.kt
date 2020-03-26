package simpleorm.core.proxy.resulsetextractor

import net.sf.cglib.proxy.*
import simpleorm.core.jdbc.ResultSetExtractor
import simpleorm.core.mapper.PrimitivesOnlyResultExtractHelper
import simpleorm.core.schema.property.IdProperty
import simpleorm.core.schema.property.PlainProperty
import simpleorm.core.schema.property.PropertyDescriptor
import java.lang.reflect.Method
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.jvm.javaMethod

class CglibRseProxyGenerator<T: Any>(
        private val propertyDescriptor: PlainProperty<T>
){

    fun create(): ResultSetExtractor<Any> {
        val enhancer = Enhancer()
        enhancer.setInterfaces(arrayOf(ResultSetExtractor::class.java))
        enhancer.setCallback(object : MethodInterceptor{

            override fun intercept(obj: Any, method: Method, args: Array<out Any>, proxy: MethodProxy): Any {
                val kClass = propertyDescriptor.kProperty.returnType.classifier as KClass<T>
                if(method.name == "mappedClass"){
                    return kClass
                }
                val resultSet = args.first() as ResultSet
                val getter = PrimitivesOnlyResultExtractHelper().getter(
                        kClass,
                        resultSet)
                val resultList = mutableListOf<T>()
                while(resultSet.next()){
                    resultList.add(getter.invoke(propertyDescriptor.column))
                }
                return resultList
            }

        })
        return enhancer.create() as ResultSetExtractor<Any>
    }

}