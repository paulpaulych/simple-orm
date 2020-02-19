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

    //fixme: разобрать этот ад
    override fun convert(resultSet: ResultSet): List<T> {
        val constructor = destClass.primaryConstructor!!
        val output = mutableListOf<T>()
        while(resultSet.next()){
            output.add(
                    constructor.callBy(
                            constructor.parameters.fold(mapOf<KParameter, Any>()) {
                                acc, kParameter ->
                                val kProperty = destClass.declaredMemberProperties.find { kParameter.name == it.name }
                                val pd = entityDescriptor.plainProperties[kProperty]
                                if(pd == null) {
                                    if(kParameter.isOptional){
                                        return@fold acc
                                    }
                                    error("property descriptor not found for constructor parameter name '${kParameter.name}'")
                                }
                                val column = pd.column
                                val value = resultSet.getterByType(kParameter.type.classifier!!)
                                        .invoke(column)
                                return@fold acc + mapOf(kParameter to value)
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