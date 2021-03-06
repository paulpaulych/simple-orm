package integration.model

import paulpaulych.utils.Open

@Open
data class Owner(
    val id: Long? = null,
    val name: String,
    val products: List<Product> = listOf()
)