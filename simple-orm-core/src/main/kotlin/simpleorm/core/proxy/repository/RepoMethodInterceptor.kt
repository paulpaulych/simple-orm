package simpleorm.core.proxy.repository

import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy
import paulpaulych.utils.LoggerDelegate
import simpleorm.core.ISimpleOrmRepo
import simpleorm.core.filter.FetchFilter
import simpleorm.core.filter.IFilterResolverRepo
import simpleorm.core.jdbc.*
import simpleorm.core.jdbc.get
import simpleorm.core.pagination.Page
import simpleorm.core.pagination.Pageable
import simpleorm.core.pagination.Sort
import simpleorm.core.proxy.ProxyGenerator
import simpleorm.core.proxy.resulsetextractor.CglibRseProxyGenerator
import simpleorm.core.schema.EntityDescriptor
import simpleorm.core.schema.property.IdProperty
import simpleorm.core.schema.property.PlainProperty
import simpleorm.core.sql.FilteringQuery
import simpleorm.core.sql.PageableQuery
import simpleorm.core.sql.Query
import simpleorm.core.sql.QueryGenerationStrategy
import simpleorm.core.sql.condition.EqualsCondition
import simpleorm.core.utils.methodByName
import java.lang.reflect.Method
import java.sql.PreparedStatement
import kotlin.reflect.KClass
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
        private val filterResolverRepo: IFilterResolverRepo
): MethodInterceptor {

    private val rse = CglibRseProxyGenerator(entityDescriptor.idProperty).create()

    private val log by LoggerDelegate()

    override fun intercept(obj: Any, method: Method, args: Array<out Any>, proxy: MethodProxy): Any? {
        if(method == ISimpleOrmRepo::class.methodByName("findById").javaMethod){
            return findById(args.first())
        }
        if(method.name == "findAll" && method.parameterCount == 0){
            return findAll()
        }
        if(method.name == "findAll" && method.parameterCount != 0 ) {
            return findAll(args.first() as Pageable)
        }
        if(method == ISimpleOrmRepo::class.methodByName("delete").javaMethod){
            return delete(args.first())
        }
        if(method == ISimpleOrmRepo::class.methodByName("save").javaMethod){
            return save(args.first())
        }
        if(method == ISimpleOrmRepo::class.methodByName("query").javaMethod){
            return query(args[0] as String, args[1] as List<Any>)
        }
        if(method.name == "findBy" && method.parameterCount == 1){
            return findBy(args.first() as FetchFilter?)
        }
        if(method.name == "findBy" && method.parameterCount == 2){
            return findBy(args.first() as FetchFilter?, args[1] as Pageable)
        }
        error("unsupported operation: ${method.name}")
    }

    private fun findById(requiredId: Any): Any? {
        log.debug("fetching ${entityDescriptor.kClass} by id = $requiredId")
        val idClass = entityDescriptor.idProperty.kProperty.returnType.classifier
        if(requiredId::class != idClass){
            throw IllegalArgumentException("required id type for ${entityDescriptor.kClass} is $idClass")
        }
        val ids = jdbc.doInConnection { conn->
            val sql = queryGenerationStrategy.select(
                    entityDescriptor.table,
                    listOf(entityDescriptor.idProperty.column),
                    listOf(EqualsCondition(entityDescriptor.idProperty.column))
            )
            val rs = conn.prepareStatement(sql)
                    .setValues(listOf(requiredId))
                    .executeQuery()
            rse.extract(rs)
        }
        if(ids.isEmpty()) return null
        return proxyGenerator.createProxyClass(entityDescriptor.kClass, requiredId)
    }

    private fun findAll(): List<Any> {
        log.debug("fetching all ${entityDescriptor.kClass}")
        val ids = jdbc.queryForList(
                queryGenerationStrategy.select(
                        entityDescriptor.table,
                        listOf(entityDescriptor.idProperty.column)
                ),
                rse::extract
        )
        return ids.map { proxyGenerator.createProxyClass(entityDescriptor.kClass, it) }
    }

    private fun findAll(pageable: Pageable): Page<Any> {

        val sorts = mapSorts(pageable.sorts)

        log.debug("fetching all ${entityDescriptor.kClass}")
        val sql = queryGenerationStrategy.pageableSelect(
                entityDescriptor.table,
                listOf(entityDescriptor.idProperty.column),
                listOf(),
                sorts
        )
        log.warn(sql)

        val ids = jdbc.doInConnection { connection ->
            val rs = connection.prepareStatement(sql)
                    .setValues(listOf(pageable.pageSize, pageable.offset))
                    .executeQuery()
            rse.extract(rs)
        }

        return Page(ids.map { proxyGenerator.createProxyClass(entityDescriptor.kClass, it) })
    }

    private fun findBy(filter: FetchFilter?): List<Any>{
        if(filter == null){
            return findAll()
        }
        val stringFilter = filterResolverRepo.getResolver(filter::class).toSql(entityDescriptor.kClass, filter, filterResolverRepo)

        val sql = FilteringQuery(
                Query(
                    entityDescriptor.table,
                    listOf(entityDescriptor.idProperty.column)
                ),
                listOf(stringFilter)
        ).toString()
        val filterParams = filter.params
        val ids = jdbc.doInConnection {connection ->
            val rs = connection.prepareStatement(sql)
                    .setValues(filterParams)
                    .executeQuery()
            rse.extract(rs)
        }
        return ids.map{ proxyGenerator.createProxyClass(entityDescriptor.kClass, it) }
    }

    private fun findBy(filter: FetchFilter?, pageable: Pageable): Page<Any>{
        if(filter == null){
            return findAll(pageable)
        }
        val sorts = mapSorts(pageable.sorts)
        val stringFilter = filterResolverRepo.getResolver(filter::class).toSql(entityDescriptor.kClass, filter, filterResolverRepo)

        val sql = PageableQuery(
                FilteringQuery(
                    Query(
                            entityDescriptor.table,
                            listOf(entityDescriptor.idProperty.column)
                    ),
                    listOf(stringFilter)
                ),
                sorts
        ).toString()
        val filterParams = filter.params
        val ids = jdbc.doInConnection {connection ->
            val rs = connection.prepareStatement(sql)
                    .setValues(filterParams + listOf(pageable.pageSize, pageable.offset))
                    .executeQuery()
            rse.extract(rs)
        }
        return Page(ids.map{ proxyGenerator.createProxyClass(entityDescriptor.kClass, it) })
    }

    private fun update(id: Any, obj: Any): Any{
        log.debug("updating existing $obj")
        val columns = mutableListOf<String>()
        val values = mutableListOf<Any?>()
        entityDescriptor.plainProperties
                .filterNot { it.value is IdProperty }
                .map {
                    it.value.column to it.key.get(obj)
                }.forEach{
                    columns.add(it.first)
                    values.add(it.second)
                }

        entityDescriptor.manyToOneProperties
                .forEach{ (_, pd) ->
                    val oneObj = pd.kProperty.get(obj)
                    columns.add(pd.foreignKeyColumn)
                    if(oneObj != null){
                        values.add(pd.manyIdProperty.get(oneObj))
                    }else{
                        values.add(null)
                    }
                }

        val sql = queryGenerationStrategy.update(
                        entityDescriptor.table,
                        columns,
                        listOf(EqualsCondition(entityDescriptor.idProperty.column))
        )
        values.add(id)

        jdbc.doInConnection{
            PreparedUpdatePSCreator(sql).create(it)
                    .setValues(values)
                    .executeUpdate()
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
                        jdbc.doInConnection{ conn ->
                            val values = listOf(
                                    id,
                                    pd.rightKeyProperty.get(right)
                                            ?: error("could not extract right key")
                            )
                            var ps = conn.prepareStatement(sql)
                                    .setValues(values)
                                    .executeUpdate()
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
                            val values = listOf(
                                    id,
                                    pd.rightKeyProperty.get(right)
                                            ?: error("could not extract right key")
                            )
                            var ps = conn.prepareStatement(sql)
                                    .setValues(values)
                                    .executeUpdate()
                        }

                    }

        }

        return findById(id)
                ?: error("row with id $id not found")
    }

    private fun insert(obj: Any): Any{
        log.debug("inserting $obj")
        val columns = mutableListOf<String>()
        val values = mutableListOf<Any?>()
        entityDescriptor.plainProperties
                .filterValues { it !is IdProperty<Any>}
                .map { it.value.column to it.key.get(obj) }
                .forEach{
                    columns.add(it.first)
                    values.add(it.second)
                }

        //TODO:дублируется код
        entityDescriptor.manyToOneProperties
                .forEach{ (_, pd) ->
                    val manyObj = pd.kProperty.get(obj) ?: return@forEach
                    columns.add(pd.foreignKeyColumn)
                    values.add(pd.manyIdProperty.get(manyObj))
                }

        val sql = queryGenerationStrategy.insert(entityDescriptor.table, columns)

        log.trace("sql: $sql")

        val idKClass = entityDescriptor.idProperty.kProperty.returnType.classifier as KClass<*>
        val id = jdbc.doInConnection{
            val ps = it.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)
                    .setValues(values)
            ps.executeUpdate()
            val keys = ps.generatedKeys
            keys.next()
            keys.get(1, idKClass)
                    ?: error("generated key is null")
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
                    val values = listOf(
                            id,
                            pd.rightKeyProperty.get(right)
                                    ?: error("could not extract right key")
                    )
                    conn.prepareStatement(sql)
                            .setValues(values)
                            .executeUpdate()
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
        log.debug("deleting ${entityDescriptor.kClass} by id = $id")
        jdbc.doInConnection { connection ->
            connection.prepareStatement(
                    queryGenerationStrategy.delete(
                            entityDescriptor.table,
                            listOf(
                                    EqualsCondition(entityDescriptor.idProperty.column)
                            )))
                    .setValues(listOf(id))
                    .executeUpdate()
        }
    }

    private fun query(sql: String, params: List<Any> = listOf()): List<Any>{
        return jdbc.doInConnection {
            val ps = it.prepareStatement(sql)
            val ids = rse.extract(
                    ps.setValues(params.toList())
                            .executeQuery()
            )
            ids.map{ id -> proxyGenerator.createProxyClass(entityDescriptor.kClass, id)}
        }
    }

    private fun mapSorts(sorts: List<Sort>): Map<String, Sort.Order> {
        return sorts.map { sort ->
            val pd = entityDescriptor.getPropertyDescriptor(sort.kProperty)
            if(pd !is PlainProperty){
                error("cannot sort by non-plain property")
            }
            pd.column to sort.order
        }.toMap()
    }
}

