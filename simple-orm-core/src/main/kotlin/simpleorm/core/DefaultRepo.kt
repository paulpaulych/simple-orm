package simpleorm.core

import simpleorm.core.jdbc.JdbcOperations
import simpleorm.core.jdbc.ResultSetExtractor
import simpleorm.core.proxy.repository.setValues
import kotlin.reflect.KClass

class DefaultRepo<T: Any, ID: Any>(
        private val jdbc: JdbcOperations,
        private val kClass: KClass<T>,
        private val rse: ResultSetExtractor<T>
): ISimpleOrmRepo<T, ID>{

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
            val rs = ps.setValues(args).executeQuery()
            rse.extract(rs)
        }
    }

}