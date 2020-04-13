package simpleorm.core.delegate

import paulpaulych.utils.LoggerDelegate
import simpleorm.core.findBy
import simpleorm.core.jdbc.JdbcOperations
import simpleorm.core.jdbc.ResultSetExtractor
import simpleorm.core.schema.property.ManyToOneProperty
import simpleorm.core.sql.QueryGenerationStrategy
import simpleorm.core.sql.condition.EqualsCondition
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1


class ManyToOnePropertyDelegate<T: Any>(
        private val pd: ManyToOneProperty<T>,
        private val id: Any,
        private val jdbc: JdbcOperations,
        private val queryGenerationStrategy: QueryGenerationStrategy,
        private val table: String,
        private val idColumnName: String,
        private val rse: ResultSetExtractor<T>
): GenericDelegate<T> {

    private val log by LoggerDelegate()

    override fun getValue(thisRef: Any, property: KProperty<*>): T? {
        val sql = queryGenerationStrategy.select(
                table,
                listOf(pd.foreignKeyColumn),
                listOf(EqualsCondition(idColumnName, id.toString())))
        val foreignKeyValue = jdbc.queryForObject(sql,rse::extract)
                ?: return null
        log.trace("manyToOne id: $foreignKeyValue. fetching by this id...")
        val res = pd.kClass.findBy(pd.kClass, pd.manyIdProperty as KProperty1<T, Any>, foreignKeyValue)
        if(res.size != 1){
            error("expected result size: 1, got: ${res.size}")
        }
        return res.first()
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        error("setting the value is prohibited")
    }

}