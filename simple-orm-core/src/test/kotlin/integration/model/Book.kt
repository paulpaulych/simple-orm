package integration.model

import paulpaulych.utils.Open

@Open
data class Book(
        val id: Long? = null,
        val name: String,
        val colors: List<Color>
)

@Open
data class Color(
        val id: Long? = null,
        val name: String
)