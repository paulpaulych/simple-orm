package simpleorm.core

import simpleorm.core.jdbc.ResultSetExtractor
import simpleorm.core.mapper.PrimitivesOnlyResultExtractHelper
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class DefaultResultSetExtractor<T: Any>(
        private val kClass: KClass<T>
): ResultSetExtractor<T> {

    private val primaryConstructor = kClass.primaryConstructor
            ?: error("$kClass has no primary constructor")

    private val constructorParameters: List<KParameter> = primaryConstructor.parameters

    override fun extract(resultSet: ResultSet): List<T> {
        val res = mutableListOf<T>()
        while (resultSet.next()){
            val args = mutableMapOf<KParameter, T>()
            constructorParameters.forEach{
                args[it] = (PrimitivesOnlyResultExtractHelper().getter(it.type.classifier as KClass<*>, resultSet).invoke(it.name!!) as T)
            }
            res += primaryConstructor.callBy(args)
        }
        return res
    }

}