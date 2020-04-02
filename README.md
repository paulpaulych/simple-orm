# simple-orm
Lightweight Kotlin ORM framework

## usage

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
(new gradle plugin may be written soon for this goal)

Then, schema.yml file is required to describe your ORM model:

```yml
entities:

  simpleorm.test.manytoone.Owner:
    table: owner
    fields:
      id:
        isId: true
        column: id
      name:
        column: name
      products:
        oneToMany:
          className: simpleorm.test.manytoone.Product
          keyField: ownerId

  simpleorm.test.manytoone.Product:
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

inline fun <reified T: Any> KClass<T>.findAll(): Collection<T>

inline fun <reified T: Any> KClass<T>.findById(id: Any): T?

inline fun <reified T: Any> KClass<T>.delete(id: Any)

inline fun <reified T: Any> save(obj: T)

```

which work in accordance with your orm schema.

Property values are loaded lazy. Every time you're calling getter
