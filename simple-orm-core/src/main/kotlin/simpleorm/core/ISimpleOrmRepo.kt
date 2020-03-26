package simpleorm.core

interface ISimpleOrmRepo<T: Any, ID: Any>{

    fun findById(id: ID): T?

    fun findAll(): List<T>

    fun save(obj: T): T

    fun delete(id: ID)

}