package integration.model

import paulpaulych.utils.Open

@Open
data class Article(
        val id: Long? = null,
        val title: String,
        val author: Author
)

@Open
data class Author(
        val id: Long? = null,
        val name: String
)

@Open
data class Woman(
        val id: Long? = null,
        val name: String,
        val husband: Man?
)

@Open
data class Man(
        val id: Long? = null,
        val name: String
)