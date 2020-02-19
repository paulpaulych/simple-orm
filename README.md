# simple-orm
Lightweight runtime Kotlin ORM framework without code generation

## usage

Firstly you need schema.yml file decribing your ORM model
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

inline fun <reified T: Any> KClass<T>.getAll(): Collection<T>

inline fun <reified T: Any> KClass<T>.getByIdLazy(id: Any): T?

inline fun <reified T: Any> KClass<T>.getById(id: Any): T?

inline fun <reified T: Any> KClass<T>.loadExtra(obj: T): T?

inline fun <reified T: Any> KClass<T>.getByParam(params: Map<KProperty1<T,*>, Any?>): Collection<T>

inline fun <reified T: Any> save(obj: T)

inline fun <reified T: Any> saveAll(values: Collection<T>)

```

which work in accordance with your orm schema.

You could look up `simple-orm-test' module to see final use case of framework. Dont't forget initiaize OrmContext!
