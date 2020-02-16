package simpleorm.core.mapper

import simpleorm.core.schema.OrmSchema
import simpleorm.core.schema.EntityDescriptor
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

class ByDescriptorBearRowMapper<T: Any>(
        private val destClass: KClass<T>,
        ormSchema: OrmSchema
): BeanRawMapper<T> {

    private val entityDescriptor: EntityDescriptor<T> = ormSchema.findEntityDescriptor(destClass)

    //fixme: это нечитабельно
    override fun convert(resultSet: ResultSet): List<T> {
        val output = mutableListOf<T>()
        while(resultSet.next()){
            output.add(
                    destClass.primaryConstructor!!.callBy(
                            destClass.primaryConstructor!!.parameters.fold(mapOf<KParameter, Any>()) {
                                acc, kParameter -> acc + mapOf(
                                    kParameter to resultSet.getterByType(kParameter.type.classifier!!)
                                            .invoke(entityDescriptor.properties[
                                                    destClass.declaredMemberProperties
                                                        .find { kParameter.name == it.name }
                                            ]?.column
                                                ?: throw RuntimeException("unknown field $kParameter.name")))
                            }
                    )
            )
        }
        return output
    }

    override fun convert(sqlResultMap: Map<String, Any>): T {
        val primaryConstructor = destClass.primaryConstructor?: error("primary constructor not found for class ${destClass.qualifiedName}")

        return primaryConstructor.callBy(
                sqlResultMap.map{ (column: String, value: Any) ->
                    val kProperty = entityDescriptor.propertyByColumn[column]
                            ?: error("property not fount by column key: $column")
                    val kParameter = (primaryConstructor.parameters.find { param: KParameter -> param.name == kProperty.name }
                            ?: error("cannot find constructor parameter ${kProperty.name}"))
                    kParameter to value
                }.toMap())
    }

}