package simpleorm

import simpleorm.core.jdbc.JdbcTemplate
import simpleorm.core.mapper.MapperFactory
import simpleorm.core.query.Filter
import simpleorm.core.query.FilteringQuery
import simpleorm.core.query.InsertStatement
import simpleorm.core.query.Query
import simpleorm.core.schema.OrmSchema
import simpleorm.core.schema.PropertyDescriptor
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

class OrmTemplate(
        private val ormSchema: OrmSchema,
        private val jdbc: JdbcTemplate
): OrmOperations{

    override fun <T : Any> getAll(kClass: KClass<T>): Collection<T>{
        val ed = ormSchema.findEntityDescriptor(kClass)
        val selectQuery = Query(
                table = ed.table,
                //fixme: уюрать небезопасный каст
                columns = ed.properties.values.map { it.column!! }
        )
        return jdbc.queryForList(selectQuery.toString(), MapperFactory(ormSchema).byDescriptorMapper(kClass))
    }

    override fun <T : Any> getByParam(kClass: KClass<T>, params: Map<KProperty1<T, *>, Any?>): Collection<T> {
        val ed = ormSchema.findEntityDescriptor(kClass)
        val query = FilteringQuery(
                query = Query(
                        ed.table,
                        //fixme: уюрать небезопасный каст
                        ed.properties.values.map { it.column!! }
                ),
                filters = params.map {
                    val column = (ed.properties[it.key]?.column
                            ?: error("property ${it.key.name} not found in entity descriptor"))
                    Filter(
                            column = column,
                            value = it.value!!
                    )
                }
        )
        return jdbc.queryForList(query.toString(), MapperFactory(ormSchema).byDescriptorMapper(kClass))
    }


    //fixme: нечитабельно
    override fun <T : Any> getById(kClass: KClass<T>, id: Any): T? {
        val ed = ormSchema.findEntityDescriptor(kClass)
        val selectQuery = FilteringQuery(
                Query(
                    table = ed.table,
                    columns = ed.properties.values.filter { it.column != null }.map { it.column!! }
                ),
                filters = listOf(
                    Filter(
                        //fixme: уюрать небезопасный каст
                        column = ed.properties.values.find { it.isId }!!.column!!,
                        value = id
                    )
                )
        )

        println(selectQuery)
        val result = jdbc.queryForObject(selectQuery.toString(), MapperFactory(ormSchema).byDescriptorMapper(kClass))!!
        return ed.properties.toList().filter { it.second.oneToMany != null }.fold(result){
            res: T, (kProperty: KProperty1<T, *>, pd: PropertyDescriptor)->
                val joinableKClass = pd.oneToMany!!.kClass
                val joinableList = joinableKClass.getByParam(
                        mapOf(pd.oneToMany.foreignKey to ed.id.get(result))
                )
                println(joinableList)
                kClass.updateValues(res, mapOf(
                        (kProperty as KProperty1<T, Any>) to joinableList
                ))
        }
    }

    override fun <T: Any> save(kClass: KClass<T>, obj: T){
        val ed = ormSchema.findEntityDescriptor(kClass)
        val insertStatement = InsertStatement(
                table = ed.table,
                values = ed.properties.map {
                    //fixme: уюрать небезопасный каст
                    (kProperty, fd) -> fd.column!! to kProperty.get(obj)
                }.toMap()
        )
        jdbc.executeUpdate(insertStatement.toString())
    }

    override fun <T : Any> saveAll(kClass: KClass<T>, obj: Collection<T>) {
        TODO("not implemented")
    }

}

private fun <T: Any> KClass<T>.updateValues(src: T, updateSrc: Map<KProperty1<T, Any>, Any?>): T{
    val constructor = this.primaryConstructor!!
    val params = constructor.parameters.fold(emptyMap()) { acc: Map<KParameter, Any?>, kParameter ->
        val newValue = updateSrc.toList().find { it.first.name == kParameter.name }
        if (newValue != null) {
            return@fold acc + mapOf(kParameter to newValue.second)
        }
        val srcVal = this.declaredMemberProperties.find { it.name == kParameter.name }
                ?. get(src)
                ?: error("property not found by constructor parameter name: ${kParameter.name}")
        acc + mapOf(kParameter to srcVal)
    }
    return constructor.callBy(params)
}