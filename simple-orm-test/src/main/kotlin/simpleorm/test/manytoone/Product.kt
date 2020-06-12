package simpleorm.test.manytoone

import paulpaulych.utils.Open

@Open
data class Product(
    val id: Long? = null,
    val name: String,
    val ownerId: Long
)