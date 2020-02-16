package simpleorm.core.mapper

import simpleorm.core.schema.EntityDescriptor
import simpleorm.core.schema.EntityDescriptorRegistry
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KParameter
import kotlin.reflect.full.createType
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

    override fun convert(map: Map<String, Any>): T {
        val primaryConstructor = destClass.primaryConstructor!!
        return primaryConstructor.callBy(
            map.map{(pname, value) ->
                primaryConstructor.parameters.filter { it.name == pname }.first() to value
            }.toMap()
        )
    }

}

fun ResultSet.getterByType(kClass: KClassifier): (String)->Any{
    return when(kClass){
        Int::class -> {s: String -> this.getInt(s)}
        String::class -> { s: String -> this.getString(s)}
        Long::class -> {s: String -> this.getInt(s)}
        Short::class -> {s: String -> this.getInt(s)}
        else -> error("cannot find extract for type: $kClass")
    }
}

class ByDescriptorBearRowMapper<T: Any>(
    private val destClass: KClass<T>,
    registry: EntityDescriptorRegistry
): BeanRawMapper<T>{

    private val entityDescriptor: EntityDescriptor = registry.findEntityDescriptor<T>(destClass.createType())

    override fun convert(resultSet: ResultSet): List<T> {
        val output = mutableListOf<T>()
        while(resultSet.next()){
            output.add(
                    destClass.primaryConstructor!!.callBy(
                            destClass.primaryConstructor!!.parameters.fold(mapOf<KParameter, Any>()) {
                                acc, prop -> acc + mapOf(
                                    prop to resultSet.getterByType(prop.type.classifier!!)
                                            .invoke(entityDescriptor.columns[prop.name]
                                                    ?: throw RuntimeException("unknown field $prop.name")))
                            }
                    )
            )
        }
        return output
    }

    override fun convert(values: Map<String, Any>): T {
        val primaryConstructor = destClass.primaryConstructor?: error("primary constructor not found for class ${destClass.qualifiedName}")

        return primaryConstructor.callBy(
                values.map{ (pname: String, value: Any) ->
                    val factName = entityDescriptor.columns[pname]
                    (primaryConstructor.parameters
                            .find { param: KParameter -> param.name == factName }
                            ?: error("unknown property")) to value
                }.toMap())
    }

}
