package simpleorm

import simpleorm.core.jdbc.JdbcTemplate
import simpleorm.core.mapper.MapperFactory
import simpleorm.core.query.Filter
import simpleorm.core.query.FilteringQuery
import simpleorm.core.query.InsertStatement
import simpleorm.core.query.Query
import simpleorm.core.schema.OrmSchema
import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties

class OrmTemplate(
        private val ormSchema: OrmSchema,
        private val jdbc: JdbcTemplate
): OrmOperations{

    override fun <T : Any> getAll(kClass: KClass<T>): Collection<T>{
        val ed = ormSchema.findEntityDescriptor(kClass)
        val selectQuery = Query(
                table = ed.table,
                columns = ed.properties.values.map { it.column }
        )
        return jdbc.queryForList(selectQuery.toString(), MapperFactory(ormSchema).byDescriptorMapper(kClass))
    }

    override fun <T : Any> getById(kClass: KClass<T>, id: Any): T? {
        val ed = ormSchema.findEntityDescriptor(kClass)
        val selectQuery = FilteringQuery(
                Query(
                    table = ed.table,
                    columns = ed.properties.values.map { it.column }
                ),
                filters = listOf(Filter(
                        column = ed.properties.values.find { it.isId }!!.column,
                        value = id
                ))
        )
        return jdbc.queryForObject(selectQuery.toString(), MapperFactory(ormSchema).byDescriptorMapper(kClass))
    }

    override fun <T: Any> save(kClass: KClass<T>, obj: T){
        val ed = ormSchema.findEntityDescriptor(kClass)
        val insertStatement = InsertStatement(
                table = ed.table,
                values = ed.properties.map {
                    (kProperty, fd) -> fd.column to kProperty.get(obj)
                }.toMap()
        )
        jdbc.executeUpdate(insertStatement.toString())
    }

    override fun <T : Any> saveAll(kClass: KClass<T>, obj: Collection<T>) {
        TODO("not implemented")
    }

}