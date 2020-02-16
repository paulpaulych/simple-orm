package simpleorm

import simpleorm.core.jdbc.JdbcTemplate
import simpleorm.core.mapper.MapperFactory
import simpleorm.core.query.Filter
import simpleorm.core.query.FilteringQuery
import simpleorm.core.query.InsertStatement
import simpleorm.core.query.Query
import simpleorm.core.schema.OrmSchemaDescriptor
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties

class OrmTemplate(
        private val ormSchemaDescriptor: OrmSchemaDescriptor,
        private val jdbc: JdbcTemplate
): OrmOperations{

    override fun <T : Any> getAll(kClass: KClass<T>): Collection<T>{
        val ed = getDescriptor(kClass)
        val selectQuery = Query(
                table = ed.table,
                columns = ed.fields.values.map { it.column }
        )
        return jdbc.queryForList(selectQuery.toString(), MapperFactory(ormSchemaDescriptor).byDescriptorMapper(kClass))
    }

    override fun <T : Any> getById(kClass: KClass<T>, id: Any): T? {
        val ed = getDescriptor(kClass)
        val selectQuery = FilteringQuery(
                Query(
                    table = ed.table,
                    columns = ed.fields.values.map { it.column }
                ),
                filters = listOf(Filter(
                        column = ed.fields.values.find { it.isId }!!.column,
                        value = id
                ))
        )
        return jdbc.queryForObject(selectQuery.toString(), MapperFactory(ormSchemaDescriptor).byDescriptorMapper(kClass))
    }

    override fun <T: Any> save(kClass: KClass<T>, obj: T){
        val ed = getDescriptor(kClass)
        val insertStatement = InsertStatement(
                table = ed.table,
                values = ed.fields.map { (fieldName, fd) ->
                    fd.column to kClass.declaredMemberProperties.find { it.name == fieldName }!!.get(obj)
                }.toMap()
        )
        jdbc.executeUpdate(insertStatement.toString())
    }

    override fun <T : Any> saveAll(kClass: KClass<T>, obj: Collection<T>) {

    }

    private fun <T: Any> getDescriptor(clazz: KClass<T>) =
        ormSchemaDescriptor.findEntityDescriptor<T>(clazz.createType())

}