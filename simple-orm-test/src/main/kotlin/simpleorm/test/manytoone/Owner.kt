package simpleorm.test.manytoone

data class Owner(
    val id: Long,
    val name: String,
    val products: List<Product> = listOf()
)