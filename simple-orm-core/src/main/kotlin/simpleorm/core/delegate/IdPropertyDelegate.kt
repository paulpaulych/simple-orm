package simpleorm.core.delegate

import paulpaulych.utils.LoggerDelegate
import kotlin.reflect.KProperty

class IdPropertyDelegate<T: Any>(
    private val id: Any
): GenericDelegate<T> {

    private val log by LoggerDelegate()

    override fun getValue(thisRef: Any, property: KProperty<*>): T? {
        return id as T
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        error("manual setting the ID is prohibited")
    }
}