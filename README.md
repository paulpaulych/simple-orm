# simple-orm
Lightweight Kotlin/JVM ORM library

## Warning

Project now is closed due to fatal management, analytics and design mistakes  

## Goal

The library was developed as a pet project for my [university assignment](https://github.com/paulpaulych/tradefirm)
just to deeply understand mechanisms of Object-relational mapping.

SimpleOrm was a great tool within given task due to its convenient API, 
but it's far from being agile and scalable product for trying to maintain and use it the future.

## Usage

If you want to persist some data classes like these:

```kotlin
import paulpaulych.utils.Open

@Open
data class Owner(
    val id: Long? = null,
    val name: String,
    val products: List<Product> = listOf()
)

@Open
data class Product(
    val id: Long? = null,
    val name: String,
    val ownerId: Long
)
```

..firstly you should mark your classes with @Open and say to kotlin-allopen-plugin make these classes open

Then, schema.yml file is required to describe your ORM model:

```yml
entities:

  integration.model.manytoone.Owner:
    table: owner
    fields:
      id:
        isId: true
        column: id
      name:
        column: name
      products:
        oneToMany:
          className: integration.model.manytoone.Product
          keyField: ownerId

  integration.model.manytoone.Product:
    table: product
    fields:
      id:
        isId: true
        column: id
      name:
        column: name
      ownerId:
        column: owner_id
```

Library provides a few generic extension functions like: 

```kotlin
inline fun <reified T: Any> KClass<T>.findById(id: Any): T?
inline fun <reified T: Any> KClass<T>.findBy(filter: FetchFilter?): List<T>
inline fun <reified T: Any> KClass<T>.findBy(filter: FetchFilter?, pageable: Pageable): Page<T>
inline fun <reified T: Any> KClass<T>.query(sql: String, params: List<Any> = listOf()): List<T>
inline fun <reified T: Any> KClass<T>.findAll(): List<T>
inline fun <reified T: Any> KClass<T>.findAll(pageable: Pageable): Page<T> 
inline fun <reified T: Any> persist(value: T): T
inline fun <reified T: Any> batchInsert(objs: List<T>): List<T>
```
which work in accordance with your orm schema.

You can see usage examples in integration tests.

SimpleOrm uses JDBC API under the hood, 
so it's possible to create bridge for Spring JdbcTemplate and use Spring Transactions for free.

Entities are proxied with CgLib. Getters and Setters are missed in Kotlin,
 so it creates delegated properties in proxy class for each entity's attribute. 
Proxying allows to load attributes by lazy. Sry but there are no more options here. Just lazy.
 Every time you call getter.
 