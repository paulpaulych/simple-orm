package simpleorm.core.mapper

import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class PropertyNameRawMapper<T: Any>(
    private val destClass: KClass<T>
): BeanRawMapper<T> {

    override fun convert(resultSet: ResultSet): List<T> {
        val output = mutableListOf<T>()
        while(resultSet.next()){
            output.add(
                destClass.primaryConstructor!!.callBy(
                    destClass.primaryConstructor!!.parameters.fold(mapOf<KParameter, Any>()) {
                            acc, prop -> acc + mapOf(prop to mapType(
                        resultSet,
                        prop.type.classifier!!
                    ).invoke(prop.name!!))
                    }
                )
            )
        }
        return output
    }

    override fun convert(map: Map<String, Any>): T {
        val primaryConstructor = destClass.primaryConstructor!!
        return primaryConstructor.callBy(
            map.map{(pname, value) ->
                primaryConstructor.parameters.filter { it.name == pname }.first() to value
            }.toMap()
        )
    }

}

fun mapType(resultSet: ResultSet, kClass: KClassifier): (String)->Any {
    return when(kClass){
        Int::class -> {s: String -> resultSet.getInt(s)}
        String::class -> { s: String -> resultSet.getString(s)}
        Long::class -> {s: String -> resultSet.getInt(s)}
        Short::class -> {s: String -> resultSet.getInt(s)}
        else -> error("cannot find extract for type: $kClass")
    }
}
