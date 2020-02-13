//package simpleorm
//
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.fasterxml.jackson.module.kotlin.convertValue
//import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
//import tradefirm.simpleorm.core.query.Filter
//import tradefirm.simpleorm.core.query.SelectQuery
//import kotlin.reflect.KClass
//import kotlin.reflect.full.createType
//
//class QueryExecutor(
//    private val entityDescriptorRegistry: EntityDescriptorRegistry,
//    private val objectMapper: ObjectMapper
//){
//
//    fun <T : Any> executeSelect(clazz: KClass<T>): List<T>{
//        val entityDescriptor = getDescriptor(clazz)
//        val selectQuery = SelectQuery(
//            table = entityDescriptor.table,
//            attributes = entityDescriptor.fields.values.toList(),
//            filters = listOf()
//        )
//        return jdbc.queryForList(selectQuery.toString(), mapOf<String, Any>(), RowMapper<T>())
//
//    }
//
//    fun <T : Any> executeParametrizedSelect(clazz: KClass<T>, params: Map<String, String>): List<T>{
//        val entityDescriptor = getDescriptor(clazz)
//        val selectQuery = SelectQuery(
//            table = entityDescriptor.table,
//            attributes = entityDescriptor.fields.values.toList(),
//            filters = params.map { Filter(it.key, it.value) }
//        )
//        val resultList = jdbc.queryForList(selectQuery.toString(), mapOf<String, Any>())
//        return resultList.map {objectMapper.convertValue(it, clazz.java)}
//    }
//
//    fun <T: Any> executeInsert(obj: T, clazz: KClass<T>){
//        val entityDescriptor = getDescriptor(clazz)
//    }
//
//    private fun <T: Any> getDescriptor(clazz: KClass<T>) =
//        entityDescriptorRegistry.entityDescriptor<Any>(clazz.createType())
//
//}
//
//inline fun <reified T: Any> T.getAll(): List<T>{
//    val queryExecutor = ApplicationContextProvider.applicationContext!!.getBean("queryExecutor") as QueryExecutor
//    return queryExecutor.executeSelect(getOuter(T::class.java)).map { jacksonObjectMapper().convertValue<T>(it) }
//}
//
//inline fun <reified T> T.getBy(constraints: Map<String, Any>): List<T>{
//    val queryExecutor = ApplicationContextProvider.applicationContext!!.getBean("queryExecutor") as QueryExecutor
//    return queryExecutor.executeParametrizedSelect(
//        getOuter(T::class.java),
//        constraints.map{it.key to it.value.toString()}.toMap()
//    ) as List<T>
//}
//
//fun <T> getOuter(companion: Class<T>): KClass<out Any>{
//    val newClassName = companion.canonicalName?.replace(".Companion", "") ?: throw RuntimeException("cannot get qualified name")
//
//    return Class.forName(newClassName).kotlin
//}
//
//fun test(){
//    Product.getBy(mapOf("id" to 1))
//}
//
///*TODO
//можно пары ключ-значение передавать в getBy,
//а можно заранее генерить фильтры(надо пошарить)
// */