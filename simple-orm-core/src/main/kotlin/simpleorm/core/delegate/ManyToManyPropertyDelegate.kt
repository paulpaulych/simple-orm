package simpleorm.core.delegate

import paulpaulych.utils.LoggerDelegate
import simpleorm.core.findBy
import simpleorm.core.jdbc.JdbcOperations
import simpleorm.core.schema.property.ManyToManyProperty
import simpleorm.core.sql.QueryGenerationStrategy
import simpleorm.core.sql.condition.EqualsCondition
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

class ManyToManyPropertyDelegate<T: Any>(
        private val pd: ManyToManyProperty<T>,
        private val id: Any,
        private val jdbc: JdbcOperations,
        private val queryGenerationStrategy: QueryGenerationStrategy
): GenericDelegate<List<T>> {

    private val log by LoggerDelegate()

    override fun getValue(thisRef: Any, property: KProperty<*>): List<T> {
        val sql = queryGenerationStrategy.select(
                pd.linkTable,
                listOf(pd.rightColumn),
                listOf(EqualsCondition(pd.leftColumn, id))
        )
        val ids = jdbc.queryForList(sql ){rse->
            val res = mutableListOf<Long>()
            while (rse.next()){
                res.add(rse.getLong(1))
            }
            res
        }
        log.trace("linked ids: $ids")
        return ids.fold(mutableListOf()){ acc, id->
            val found = pd.kClass.findBy(
                pd.kClass,
                pd.rightKeyProperty as KProperty1<T, Any>,
                id
            )
            if(found.isEmpty()){
                error("right side with id = $id not found")
            }
            acc.addAll(found)
            acc
        }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: List<T>?) {
        error("setting the value is prohibited")
    }

}