package simpleorm.core.delegate

import paulpaulych.utils.LoggerDelegate
import simpleorm.core.jdbc.JdbcOperations
import simpleorm.core.jdbc.ResultSetExtractor
import simpleorm.core.proxy.resulsetextractor.CglibRseProxyGenerator
import simpleorm.core.sql.QueryGenerationStrategy
import simpleorm.core.sql.condition.EqualsCondition
import simpleorm.core.schema.EntityDescriptor
import simpleorm.core.schema.property.*

class JdbcDelegateCreator(
     private val jdbc: JdbcOperations,
     private val queryGenerationStrategy: QueryGenerationStrategy
): IDelegateCreator {

    private val log by LoggerDelegate()

    override fun <T : Any, R: Any> create(ed: EntityDescriptor<R>, pd: PropertyDescriptor<T>, initialValue: Any): GenericDelegate<T> {
        when(pd) {
            is IdProperty<T> -> {
                log.trace("creating idPropertyDelegate for ${pd.kProperty}...")
                return IdPropertyDelegate(initialValue)
            }
            is PlainProperty<T> -> {
                log.trace("creating PlainPropertyDelegate for ${pd.kProperty}...")
                @Suppress("UNCHECKED_CAST")
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
            is OneToManyProperty<T> -> {
                log.trace("creating OneToManyPropertyDelegate for ${pd.kProperty}...")
                @Suppress("UNCHECKED_CAST")
                return OneToManyPropertyDelegate(
                        pd,
                        initialValue
                ) as GenericDelegate<T>
            }
            is ManyToManyProperty<T> -> {
                log.trace("creating ManyToManyPropertyDelegate for ${pd.kProperty}...")
                @Suppress("UNCHECKED_CAST")
                return ManyToManyPropertyDelegate(
                        pd,
                        initialValue,
                        jdbc,
                        queryGenerationStrategy
                ) as GenericDelegate<T>
            }
            is ManyToOneProperty<T> -> {
                log.trace("creating ManyToOnePropertyDelegate for ${pd.kProperty}...")
                @Suppress("UNCHECKED_CAST")
                return ManyToOnePropertyDelegate(
                        pd,
                        initialValue,
                        jdbc,
                        queryGenerationStrategy,
                        ed.table,
                        ed.idProperty.column,
                        CglibRseProxyGenerator(
                                OneToManyPlainAdapter(pd as ManyToOneProperty<Any>)
                        ).create() as ResultSetExtractor<T>
                )
            }
            else -> error("unknown property descriptor type ${pd::class}")
        }
    }
}

class OneToManyPlainAdapter(
        p: ManyToOneProperty<Any>
) : PlainProperty<Any>(p.manyIdProperty, p.foreignKeyColumn)
