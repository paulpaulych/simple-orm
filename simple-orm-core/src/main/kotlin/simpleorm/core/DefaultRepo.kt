package simpleorm.core

import simpleorm.core.jdbc.JdbcOperations
import simpleorm.core.jdbc.ResultSetExtractor
import simpleorm.core.proxy.repository.PreparedStatementValuesSetter
import kotlin.reflect.KClass

class DefaultRepo<T: Any, ID: Any>(
        private val jdbc: JdbcOperations,
        private val kClass: KClass<T>,
        private val rse: ResultSetExtractor<T>
): ISimpleOrmRepo<T, ID>{

//    private val table = kClass.simpleName
//            ?: error("$kClass has no simple name")
//
//    private val properties: List<KProperty1<T, *>>
//
//    init {
//        val primaryConstructor = kClass.primaryConstructor
//                ?: error("$kClass has no primary constructor")
//        properties = primaryConstructor.parameters.map {
//            kClass.declaredMemberProperties.find { p-> p.name == it.name }
//                    ?: error("'${it.name}' property not found in $kClass. " +
//                            "All primary constructor parameters should be present as property")
//        }
//    }

    override fun findById(id: ID): T? {
        error("operation is unsupported for $kClass. Use custom query instead")
    }

    override fun findAll(): List<T> {
        error("operation is unsupported for $kClass. Use custom query instead")
    }

    override fun save(obj: T): T {
        error("operation is unsupported for $kClass. Use custom query instead")
    }

    override fun delete(id: ID) {
        error("operation is unsupported for $kClass. Use custom query instead")
    }

    override fun query(sql: String, args: List<Any>): List<T> {
        return jdbc.doInConnection {
            val ps = it.prepareStatement(sql)
            val rs = PreparedStatementValuesSetter(args).set(ps).executeQuery()
            rse.extract(rs)
        }
    }

}