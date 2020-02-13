package paulpaulych.utils

import com.sun.org.slf4j.internal.Logger
import com.sun.org.slf4j.internal.LoggerFactory
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


class LoggerDelegate : ReadOnlyProperty<Any?, Logger> {

    companion object {
        private fun <T>createLogger(clazz: Class<T>) : Logger {
            return LoggerFactory.getLogger(clazz)
        }
    }

    private var logger: Logger? = null

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): Logger {
        if (logger == null) {
            logger = createLogger(thisRef!!.javaClass)
        }
        return logger!!
    }

}
