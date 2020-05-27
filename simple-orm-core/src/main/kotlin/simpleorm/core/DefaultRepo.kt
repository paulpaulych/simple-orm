package simpleorm.core

import simpleorm.core.filter.EqKPropertyFilter
import simpleorm.core.filter.FetchFilter
import simpleorm.core.filter.IFilterResolverRepo
import simpleorm.core.filter.ParameterizableFetchFilter
import simpleorm.core.jdbc.JdbcOperations
import simpleorm.core.jdbc.get
import simpleorm.core.jdbc.setValues
import simpleorm.core.pagination.Page
import simpleorm.core.pagination.Pageable
import simpleorm.core.pagination.Sort
import simpleorm.core.schema.naming.INamingStrategy
import simpleorm.core.schema.naming.SnakeCaseNamingStrategy
import simpleorm.core.sql.FilteringQuery
import simpleorm.core.sql.PageableQuery
import simpleorm.core.sql.Query
import simpleorm.core.sql.QueryGenerationStrategy
import simpleorm.core.sql.condition.EqualsCondition
import java.sql.PreparedStatement
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

class DefaultRepo<T: Any, ID: Any>(
        private val jdbc: JdbcOperations,
        private val kClass: KClass<T>,
        private val namingStrategy: INamingStrategy,
        private val queryGenerationStrategy: QueryGenerationStrategy,
        private val filterResolverRepo: IFilterResolverRepo
): ISimpleOrmRepo<T, ID>{

    private val primaryConstructor: KFunction<T>
    private val rse = DefaultResultSetExtractor(kClass, namingStrategy)
    private val table: String
    private val columns: List<String>
    private val idProperty: KProperty1<T, *>

    init {
        if(!kClass.isData){
            error("$kClass must be data class to be persistent")
        }
        this.primaryConstructor = kClass.primaryConstructor
                ?: error("$kClass must have primary constructor to be persistent")
        this.table = namingStrategy.toTableName(kClass.simpleName
                ?: error("$kClass has no simple name"))
        this.columns = primaryConstructor.parameters.map {
            namingStrategy.toColumnName(
                    it.name ?: error("$it has no name")
            )
        }
        this.idProperty = kClass.memberProperties.find { it.name == "id" }
                ?: error("$kClass must have id property or be described in orm schema to be persistent")
        val idPropertyKClass = idProperty.returnType.classifier as KClass<*>
        if(idPropertyKClass != Long::class && idPropertyKClass != Int::class){
            error("default id cannot be $idPropertyKClass. It must be Long. Change id type or describe entity in orm schema")
        }
    }

    override fun findById(id: ID): T? {
        val idProperty = kClass.memberProperties.find { it.name == "id" }
                ?: error("$kClass must have id property or be described in orm schema to be persistent")
        val filter = EqKPropertyFilter(idProperty, id)
        val sql = FilteringQuery(
                Query(table, columns),
                listOf(filterResolverRepo.getResolver(filter::class).toSql(kClass, filter))
        ).toString()
        val result = jdbc.doInConnection { connection ->
            val rs = connection.prepareStatement(sql)
                    .setValues(listOf(id))
                    .executeQuery()
            rse.extract(rs)
        }
        return result.firstOrNull()
    }

    override fun findAll(): List<T> {
        return jdbc.queryForList(
                Query(table, columns).toString(),
                rse::extract
        )
    }

    override fun save(obj: T): T {
        val id = idProperty.get(obj)
        if(id != null){
            return update(obj, id as ID)
        }
        return insert(obj)
    }

    private fun insert(obj: T): T{
        val columns = mutableListOf<String>()
        val values = mutableListOf<Any?>()
        kClass.declaredMemberProperties.forEach{ kProperty ->
            columns.add(namingStrategy.toColumnName(kProperty.name))
            values.add(kProperty.get(obj))
        }
        val sql = queryGenerationStrategy.insert(table, columns)

        val id = jdbc.doInConnection{
            val ps = it.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)
                    .setValues(values)
            ps.executeUpdate()
            val keys = ps.generatedKeys
            keys.next()
            keys.get(1, Long::class)
                    ?: error("generated key is null")
        }
        //fixme: insert и select должны быть в транзации
        return findById(id as ID)
                ?: error("findById() failed. please report ti " )
    }

    private fun update(obj: T, id: ID): T{
        val columns = mutableListOf<String>()
        val values = mutableListOf<Any?>()
        kClass.memberProperties.forEach{ kProperty ->
            columns.add(namingStrategy.toColumnName(kProperty.name))
            values.add(kProperty.get(obj))
        }
        val idColumn = namingStrategy.toColumnName(idProperty.name)
        val sql = queryGenerationStrategy.update(table, columns, listOf(EqualsCondition(idColumn, id)))

        jdbc.doInConnection{
            jdbc.doInConnection{ connection->
                connection.prepareStatement(sql)
                        .setValues(values)
                        .executeUpdate()
            }
        }
        //fixme: insert и select должны быть в транзации
        return findById(id)
                ?: error("findById() failed. please report to maintainers" )
    }

    override fun delete(id: ID) {
        val idColumn = namingStrategy.toColumnName(idProperty.name)
        val sql = queryGenerationStrategy.delete(table, listOf(EqualsCondition(idColumn, id)))
        jdbc.execute(sql)
    }

    override fun query(sql: String, args: List<Any>): List<T> {
        return jdbc.doInConnection {
            val ps = it.prepareStatement(sql)
            val rs = ps.setValues(args).executeQuery()
            rse.extract(rs)
        }
    }

    override fun findAll(pageable: Pageable): Page<T> {
        return findBy(listOf(), pageable)
    }

    override fun findBy(filters: List<FetchFilter>): List<T>{
        val stringFilters = filters.map { filter ->
            filterResolverRepo.getResolver(filter::class).toSql(kClass, filter)
        }
        val filterParams = filters.filterIsInstance(ParameterizableFetchFilter::class.java).map { it.value }
        val sql = FilteringQuery(
                Query(table, columns),
                stringFilters
        ).toString()
        return jdbc.doInConnection {connection ->
            val rs = connection.prepareStatement(sql)
                    .setValues(filterParams)
                    .executeQuery()
            rse.extract(rs)
        }

    }

    override fun findBy(filters: List<FetchFilter>, pageable: Pageable): Page<T> {
        val stringFilters = filters.map { filter ->
            filterResolverRepo.getResolver(filter::class).toSql(kClass, filter)
        }
        val sql = PageableQuery(
                FilteringQuery(
                        Query(table, columns),
                        stringFilters
                ),
                mapSorts(pageable.sorts)
        ).toString()
        val filterParams = filters.filterIsInstance(ParameterizableFetchFilter::class.java).map { it.value }
        val result = jdbc.doInConnection {connection ->
            val rs = connection.prepareStatement(sql)
                    .setValues(filterParams + listOf(pageable.pageSize, pageable.offset))
                    .executeQuery()
            rse.extract(rs)
        }
        return Page(result)
    }


    private fun mapSorts(sorts: List<Sort>): Map<String, Sort.Order> {
        return sorts.map { sort ->
            val column = namingStrategy.toColumnName(sort.kProperty.name)
            column to sort.order
        }.toMap()
    }
}