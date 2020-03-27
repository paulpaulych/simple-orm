package simpleorm.core.delegate

import paulpaulych.utils.LoggerDelegate
import simpleorm.core.jdbc.JdbcOperations
import simpleorm.core.proxy.resulsetextractor.CglibRseProxyGenerator
import simpleorm.core.sql.QueryGenerationStrategy
import simpleorm.core.sql.condition.EqualsCondition
import simpleorm.core.schema.EntityDescriptor
import simpleorm.core.schema.property.IdProperty
import simpleorm.core.schema.property.OneToManyProperty
import simpleorm.core.schema.property.PlainProperty
import simpleorm.core.schema.property.PropertyDescriptor

class JdbcDelegateCreator(
     private val jdbc: JdbcOperations,
     private val queryGenerationStrategy: QueryGenerationStrategy
): IDelegateCreator {

    private val log by LoggerDelegate()

    override fun <T : Any, R: Any> create(ed: EntityDescriptor<R>, pd: PropertyDescriptor<T>, initialValue: Any): GenericDelegate<T> {
        if(pd is IdProperty<T>){
            log.trace("creating idPropertyDelegate for ${pd.kProperty}...")
            return IdPropertyDelegate(initialValue)
        }
        if(pd is PlainProperty<T>){
            log.trace("creating PlainPropertyDelegate for ${pd.kProperty}...")
            return PlainPropertyDelegate(
                    jdbc,
                    queryGenerationStrategy.select(
                            ed.table,
                            listOf(pd.column),
                            listOf(EqualsCondition(ed.idProperty.column, "?"))
                    ),
                    initialValue,
                    CglibRseProxyGenerator(pd).create()
            ) as GenericDelegate<T>
        }
        if(pd is OneToManyProperty<T>){
            log.trace("creating OneToManyPropertyDelegate for ${pd.kProperty}...")
            return OneToManyPropertyDelegate(
                pd,
                initialValue
            ) as GenericDelegate<T>
        }
        error("unknown property descriptor type ${pd::class}")
    }

}
