package simpleorm.core

import simpleorm.core.jdbc.ResultSetExtractor
import simpleorm.core.jdbc.get
import simpleorm.core.schema.naming.INamingStrategy
import simpleorm.core.schema.naming.SnakeCaseNamingStrategy
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class DefaultResultSetExtractor<T: Any>(
        kClass: KClass<T>,
        private val namingStrategy: INamingStrategy
            = SnakeCaseNamingStrategy()
): ResultSetExtractor<T> {

    private val primaryConstructor = kClass.primaryConstructor
            ?: error("$kClass has no primary constructor")

    private val constructorParameters: List<KParameter> = primaryConstructor.parameters

    override fun extract(resultSet: ResultSet): List<T> {
        val res = mutableListOf<T>()
        while (resultSet.next()){
            val args = mutableMapOf<KParameter, T>()
            constructorParameters.forEach{
                val constructorParameterName = it.name
                        ?: error("constructor parameter $it has no name")
                val columnName = namingStrategy.toColumnName(constructorParameterName)
                args[it] = resultSet.get(columnName, it.type.classifier as KClass<*>) as T
            }
            res += primaryConstructor.callBy(args)
        }
        return res
    }

}