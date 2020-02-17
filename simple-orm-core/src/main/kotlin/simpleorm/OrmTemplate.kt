package simpleorm

import simpleorm.core.jdbc.JdbcTemplate
import simpleorm.core.mapper.MapperFactory
import simpleorm.core.query.filter.EqualsFilter
import simpleorm.core.query.FilteringQuery
import simpleorm.core.query.InsertStatement
import simpleorm.core.query.Query
import simpleorm.core.schema.OrmSchema
import simpleorm.core.schema.property.OneToManyProperty
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
                columns = ed.plainProperties.values.map { it.column }
        )
        return jdbc.queryForList(selectQuery.toString(), MapperFactory(ormSchema).byDescriptorMapper(kClass))
    }

    override fun <T : Any> getByParam(kClass: KClass<T>, params: Map<KProperty1<T, *>, Any?>): Collection<T> {
        val ed = ormSchema.findEntityDescriptor(kClass)
        val query = FilteringQuery(
                query = Query(
                        ed.table,
                        ed.plainProperties.values.map { it.column }
                ),
                filters = params.map {
                    val column = (ed.plainProperties[it.key]?.column
                            ?: error("property ${it.key.name} not found in entity descriptor"))
                    EqualsFilter(
                            column = column,
                            value = it.value!!
                    )
                }
        )
        return jdbc.queryForList(query.toString(), MapperFactory(ormSchema).byDescriptorMapper(kClass))
    }

    override fun <T : Any> getByIdLazy(kClass: KClass<T>, id: Any): T? {
        val ed = ormSchema.findEntityDescriptor(kClass)
        val selectQuery =
            FilteringQuery(
                Query(
                    table = ed.table,
                    columns = ed.plainProperties.values.map { it.column }
                ),
                filters = listOf(
                        EqualsFilter(
                                //fixme: уюрать небезопасный каст
                                column = ed.idProperty.second.column,
                                value = id
                        )
                )
            )
        return jdbc.queryForObject(selectQuery.toString(), MapperFactory(ormSchema).byDescriptorMapper(kClass))!!
    }

    override fun <T : Any> getById(kClass: KClass<T>, id: Any): T? {
        val byId = getByIdLazy(kClass, id) ?: return null
        return loadExtra(kClass, byId)
    }

    override fun <T: Any> save(kClass: KClass<T>, obj: T){
        val ed = ormSchema.findEntityDescriptor(kClass)
        val insertStatement = InsertStatement(
                table = ed.table,
                values = ed.plainProperties.map {
                    //fixme: уюрать небезопасный каст
                    (kProperty, fd) -> fd.column to kProperty.get(obj)
                }.toMap()
        )
        jdbc.executeUpdate(insertStatement.toString())
    }

    override fun <T : Any> saveAll(kClass: KClass<T>, obj: Collection<T>) {
        TODO("not implemented")
    }

    override fun <T : Any> loadExtra(kClass: KClass<T>, obj: T): T {
        val ed = ormSchema.findEntityDescriptor(kClass)

        return ed.oneToManyProperties.toList().fold(obj){
            res: T, (kProperty: KProperty1<T, *>, pd: OneToManyProperty )->

            val newValueKClass = pd.kClass
            val newValue = newValueKClass.getByParam(
                    mapOf(pd.foreignKey to ed.idProperty.first.get(obj))
            )
            kClass.updateValues(res, mapOf(kProperty to newValue))
        }
    }
}

private fun <T: Any> KClass<T>.updateValues(src: T, updateSrc: Map<KProperty1<T, *>, *>): T{
    val constructor = this.primaryConstructor!!
    val params = constructor.parameters.fold(emptyMap()) { acc: Map<KParameter, *>, kParameter ->
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