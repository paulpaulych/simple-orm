package simpleorm.core.mapper

import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class SimplePropertyRawMapper<T: Any>(
    private val destClass: KClass<T>
): BeanRawMapper<T> {

    override fun convert(resultSet: ResultSet): List<T> {
        val output = mutableListOf<T>()
        while(resultSet.next()){
            output.add(
                destClass.primaryConstructor!!.callBy(
                    destClass.primaryConstructor!!.parameters.fold(mapOf<KParameter, Any>()) {
                            acc, prop -> acc + mapOf(
                            prop to resultSet.getterByType(prop.type.classifier!!)
                                    .invoke(prop.name!!))
                    }
                )
            )
        }
        return output
    }

    override fun convert(sqlResultMap: Map<String, Any>): T {
        val primaryConstructor = destClass.primaryConstructor!!
        return primaryConstructor.callBy(
            sqlResultMap.map{ (pname, value) ->
                primaryConstructor.parameters.filter { it.name == pname }.first() to value
            }.toMap()
        )
    }

}
