package simpleorm.core

import kotlin.reflect.KProperty1

interface ISimpleOrmRepo<T: Any, ID: Any>{

    fun findById(id: ID): T?

    fun findAll(): List<T>

    fun save(obj: T): T

    fun delete(id: ID)

    fun query(sql: String, args: List<Any>): List<T>

    fun findBy(spec: Map<KProperty1<T, Any>, Any>): List<T>
}