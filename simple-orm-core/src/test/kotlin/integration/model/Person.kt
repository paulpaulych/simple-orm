package integration.model

import paulpaulych.utils.Open

@Open
data class Person (
    val id: Long?,
    var name: String,
    var age: Int
)