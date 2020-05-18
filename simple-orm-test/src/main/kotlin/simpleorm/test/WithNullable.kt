package simpleorm.test

import paulpaulych.utils.Open

@Open
data class WithNullable (
        val id: Long? = null,
        val opt: String? = null
)