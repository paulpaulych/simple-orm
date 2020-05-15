package simpleorm.core.pagination

class PageRequest(
        override val pageNumber: Int,
        override val pageSize: Int,
        override val sorts: List<Sort>
) : Pageable{
    override val first: Pageable
        get() = PageRequest(0, pageSize, sorts)
    override val next: Pageable
        get() = PageRequest(pageNumber + 1, pageSize, sorts)
    override val prev: Pageable?
        get() {
            if(pageNumber - 1 < 0){
                return null
            }
            return PageRequest(pageNumber - 1, pageSize, sorts)
        }
    override val offset: Int = pageNumber * pageSize
}
