package simpleorm.core.pagination

interface Pageable {
    val first: Pageable
    val next: Pageable
    val prev: Pageable?
    val offset: Int
    val pageSize: Int
    val pageNumber: Int
    val sorts: List<Sort>
}

