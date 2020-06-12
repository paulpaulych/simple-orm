package simpleorm.test

import paulpaulych.utils.Open

@Open
data class Person (
    val id: Long?,
    var name: String,
    var age: Int
)