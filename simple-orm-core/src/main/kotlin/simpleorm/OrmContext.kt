package simpleorm

class OrmContext(
    val ormTemplate: OrmTemplate
)

object OrmContextProvider{
    var ormContext : OrmContext? = null
}
