package simpleorm.core

import simpleorm.core.filter.FetchFilter
import simpleorm.core.pagination.Page
import simpleorm.core.pagination.Pageable

interface ISimpleOrmRepo<T: Any, ID: Any>{

    fun findById(id: ID): T?
    fun findAll(): List<T>
    fun findAll(pageable: Pageable): Page<T>
    fun findBy(filter: FetchFilter): List<T>
    fun findBy(filter: FetchFilter, pageable: Pageable): Page<T>
    fun save(obj: T): T
    fun delete(id: ID)
    fun query(sql: String, args: List<Any>): List<T>
}