package simpleorm.core.proxy.repository

import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy
import paulpaulych.utils.LoggerDelegate
import simpleorm.core.ISimpleOrmRepo
import simpleorm.core.jdbc.JdbcOperations
import simpleorm.core.jdbc.ResultSetExtractor
import simpleorm.core.proxy.ProxyGenerator
import simpleorm.core.proxy.resulsetextractor.CglibRseProxyGenerator
import simpleorm.core.schema.EntityDescriptor
import simpleorm.core.schema.property.IdProperty
import simpleorm.core.schema.property.PlainProperty
import simpleorm.core.sql.QueryGenerationStrategy
import simpleorm.core.sql.UpdateStatement
import simpleorm.core.sql.condition.EqualsCondition
import simpleorm.core.utils.method
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.jvm.javaMethod

/**
 * Cglib Interceptor for [ISimpleOrmRepo]
 */
class RepoMethodInterceptor(
        private val entityDescriptor: EntityDescriptor<*>,
        private val jdbc: JdbcOperations,
        private val queryGenerationStrategy: QueryGenerationStrategy,
        private val proxyGenerator: ProxyGenerator,
        private val idGenerator: ()->Any
): MethodInterceptor {

    private val rse = CglibRseProxyGenerator(entityDescriptor.idProperty).create()

    private val log by LoggerDelegate()

    override fun intercept(obj: Any, method: Method, args: Array<out Any>, proxy: MethodProxy): Any? {
        if(method == ISimpleOrmRepo::class.method("findById").javaMethod){
            return findById(args.first())
        }
        if(method == ISimpleOrmRepo::class.method("findAll").javaMethod){
            return findAll()
        }
        if(method == ISimpleOrmRepo::class.method("delete").javaMethod){
            return delete(args.first())
        }
        if(method == ISimpleOrmRepo::class.method("save").javaMethod){
            return save(args.first())
        }
        error("unsupported operation: ${method.name}")
    }

    private fun findById(requiredId: Any): Any? {
        val idClass = entityDescriptor.idProperty.kProperty.returnType.classifier
        if(requiredId::class != idClass){
            throw IllegalArgumentException("required id type is $idClass")
        }
        val id = jdbc.queryForObject(
                queryGenerationStrategy.select(
                        entityDescriptor.table,
                        listOf(entityDescriptor.idProperty.column),
                        listOf(EqualsCondition(entityDescriptor.idProperty.column, requiredId))
                ),
                rse
        )
        id?:let {
            return null
        }
        return proxyGenerator.createProxyClass(entityDescriptor.kClass, requiredId)
    }

    private fun findAll(): List<Any> {
        val ids = jdbc.queryForList(
                queryGenerationStrategy.select(
                        entityDescriptor.table,
                        listOf(entityDescriptor.idProperty.column)
                ),
                rse
        )
        return ids.map { proxyGenerator.createProxyClass(entityDescriptor.kClass, it) }
    }

    private fun update(id: Any, obj: Any): Any{
        val columns = mutableListOf<String>()
        val values = mutableListOf<String>()
        entityDescriptor.plainProperties
                .filterNot { it.value is IdProperty }
                .map {
                    it.value.column to it.key.get(obj).toString()
                }.forEach{
                    columns.add(it.first)
                    values.add(it.second)
                }
        jdbc.executeUpdate(
                queryGenerationStrategy.update(
                        entityDescriptor.table,
                        columns,
                        values,
                        listOf(EqualsCondition(entityDescriptor.idProperty.column, id.toString()))
                )
        )
        return findById(id)!!
    }


    private fun insert(obj: Any): Any{
        val id = idGenerator.invoke()

        val columns = mutableListOf<String>()
        val values = mutableListOf<String>()
        entityDescriptor.plainProperties
                .map {
                    if(it.value is IdProperty<*>)
                        it.value.column to  id.toString()
                    else it.value.column to it.key.get(obj).toString()
                }.forEach{
                    columns.add(it.first)
                    values.add(it.second)
                }

        jdbc.executeUpdate(
                queryGenerationStrategy.insert(
                        entityDescriptor.table,
                        columns,
                        values
                )
        )
        return findById(id)!!
    }

    private fun save(obj: Any): Any {

        val oldId = entityDescriptor.idProperty.kProperty.get(obj)
                ?: return insert(obj)
        return update(oldId, obj)

    }

    private fun delete(id: Any) {
        jdbc.executeUpdate(
                queryGenerationStrategy.delete(
                    entityDescriptor.table,
                    listOf(
                        EqualsCondition(
                            entityDescriptor.idProperty.column,
                            id
                        )
                    )
        ))
    }


}