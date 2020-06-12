package simpleorm.core.delegate

import paulpaulych.utils.LoggerDelegate
import simpleorm.core.jdbc.JdbcOperations
import simpleorm.core.jdbc.ResultSetExtractor
import kotlin.reflect.KProperty

class PlainPropertyDelegate<T : Any>(
        private val jdbc: JdbcOperations,
        private val sql: String,
        private val id: Any,
        private val rse: ResultSetExtractor<T>
) : GenericDelegate<T> {

    private val log by LoggerDelegate()

    private var value: T? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): T? {
        log.trace("delegate getter invoked for $property")
        return jdbc.queryForObject(sql.replace("?", id.toString()), rse::extract)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        this.value = value
    }
}

