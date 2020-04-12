package simpleorm.core.proxy.repository

import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy
import paulpaulych.utils.LoggerDelegate
import simpleorm.core.ISimpleOrmRepo
import simpleorm.core.jdbc.*
import simpleorm.core.proxy.ProxyGenerator
import simpleorm.core.proxy.resulsetextractor.CglibRseProxyGenerator
import simpleorm.core.schema.EntityDescriptor
import simpleorm.core.schema.property.IdProperty
import simpleorm.core.sql.QueryGenerationStrategy
import simpleorm.core.sql.condition.EqualsCondition
import simpleorm.core.utils.method
import java.lang.reflect.Method
import java.sql.PreparedStatement
import kotlin.reflect.jvm.javaMethod
import simpleorm.core.save as saveGlobal

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
        if(method == ISimpleOrmRepo::class.method("query").javaMethod){
            return query(args[0] as String, args[1] as List<Any>)
        }
        error("unsupported operation: ${method.name}")
    }

    private fun findById(requiredId: Any): Any? {

        val idClass = entityDescriptor.idProperty.kProperty.returnType.classifier
        if(requiredId::class != idClass){
            throw IllegalArgumentException("required id type for ${entityDescriptor.kClass} is $idClass")
        }
        val id = jdbc.queryForObject(
                queryGenerationStrategy.select(
                        entityDescriptor.table,
                        listOf(entityDescriptor.idProperty.column),
                        listOf(EqualsCondition(entityDescriptor.idProperty.column, requiredId))
                ),
                rse::extract
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
                rse::extract
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
        val sql = queryGenerationStrategy.update(
                        entityDescriptor.table,
                        columns,
                        listOf(EqualsCondition(entityDescriptor.idProperty.column, id.toString()))
        )

        jdbc.doInConnection{
            var ps = PreparedUpdatePSCreator(sql).create(it)
            ps = PreparedStatementValuesSetter(values)
                    .set(ps)
            ps.executeUpdate()
        }

        entityDescriptor.oneToManyProperties.forEach{
            saveGlobal(it.value.kClass, it.key.get(obj) as Any)
        }

        entityDescriptor.manyToManyProperties.forEach {
            //внесем изменения в связывающую таблицу
            val beforeObj = findById(id)
                    ?: error("before obj does not exist")

            val pd = it.value
            val beforeRights = pd.kProperty.get(beforeObj) as List<Any>
            val requiredRights = pd.kProperty.get(obj) as List<Any>

            //добавим недостающее
            requiredRights.filterNot(beforeRights::contains)
                    .forEach { right->
                        log.trace("creating link to $right")
                        val sql = queryGenerationStrategy.insert(
                                pd.linkTable,
                                listOf(pd.leftColumn, pd.rightColumn)
                        )
                        log.trace("executing: $sql")
                        jdbc.doInConnection{
                            var ps = it.prepareStatement(sql)
                            val values = listOf(
                                    id,
                                    pd.rightKeyProperty.get(right)
                                            ?: error("could not extract right key")
                            )
                            ps = PreparedStatementValuesSetter(values)
                                    .set(ps)
                            ps.executeUpdate()
                        }
                    }
            //удалим лишние
            beforeRights.filterNot(requiredRights::contains)
                    .forEach { right->
                        log.trace("removing link to $right")
                        val sql = queryGenerationStrategy.delete(
                                pd.linkTable,
                                listOf(
                                        EqualsCondition(pd.leftColumn),
                                        EqualsCondition(pd.rightColumn)
                                )
                        )
                        jdbc.doInConnection{ conn ->
                            var ps = conn.prepareStatement(sql)
                            val values = listOf(
                                    id,
                                    pd.rightKeyProperty.get(right)
                                            ?: error("could not extract right key")
                            )
                            ps = PreparedStatementValuesSetter(values)
                                    .set(ps)
                            ps.executeUpdate()
                        }

                    }

        }

        return findById(id)
                ?: error("row with id $id not found")
    }

    private fun insert(obj: Any): Any{

        val columns = mutableListOf<String>()
        val values = mutableListOf<String>()
        entityDescriptor.plainProperties
                .filterValues { it !is IdProperty<Any>}
                .map { it.value.column to it.key.get(obj).toString() }
                .forEach{
                    columns.add(it.first)
                    values.add(it.second)
                }

        val sql = queryGenerationStrategy.insert(entityDescriptor.table, columns)

        val id = jdbc.doInConnection{
            var ps = it.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)
            ps = PreparedStatementValuesSetter(values)
                    .set(ps)
            ps.executeUpdate()
            val keys = ps.generatedKeys
            keys.next()
            keys.getLong(1)
        }

        entityDescriptor.oneToManyProperties.values.forEach{pd->
            val manyValues = pd.kProperty.get(obj) as Iterable<Any>
            manyValues.forEach{
                saveGlobal(pd.kClass, it)
            }
        }

        entityDescriptor.manyToManyProperties.forEach {
            //внесем изменения в связывающую таблицу
            val pd = it.value
            val requiredRights = pd.kProperty.get(obj) as List<Any>
            //добавим недостающее
            requiredRights.forEach {right->
                val sql = queryGenerationStrategy.insert(
                        pd.linkTable,
                        listOf(pd.leftColumn, pd.rightColumn)
                )
                jdbc.doInConnection {conn->
                    var ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)
                    val values = listOf(
                            id,
                            pd.rightKeyProperty.get(right)
                                    ?: error("could not extract right key")
                    )
                    ps = PreparedStatementValuesSetter(values)
                            .set(ps)
                    ps.executeUpdate()
                }
            }
        }
        return findById(id)!!
    }

    private fun save(obj: Any): Any {
        log.trace("save called for $obj")

        val oldId = entityDescriptor.idProperty.kProperty.get(obj)
                ?: return insert(obj)
        return update(oldId, obj)

    }

    private fun delete(id: Any) {
        jdbc.update(
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

    private fun query(sql: String, params: List<Any> = listOf()): List<Any>{
        return jdbc.doInConnection {
            val ps = it.prepareStatement(sql)
            val ids = rse.extract(
                    PreparedStatementValuesSetter(params.toList())
                            .set(ps)
                            .executeQuery()
            )
            ids.map{ id -> proxyGenerator.createProxyClass(entityDescriptor.kClass, id)}
        }
    }

}

