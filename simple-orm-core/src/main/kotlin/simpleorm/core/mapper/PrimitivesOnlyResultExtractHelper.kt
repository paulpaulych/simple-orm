package simpleorm.core.mapper

import java.math.BigDecimal
import java.sql.ResultSet
import kotlin.reflect.KClass

class PrimitivesOnlyResultExtractHelper: ResultExtractHelper {

    override fun <T: Any> getter(kClass: KClass<T>, resultSet: ResultSet): (String) -> T {
        return when(kClass) {
            Int::class -> { s: String -> resultSet.getInt(s) as T}
            String::class -> { s: String -> resultSet.getString(s) as T}
            Float::class -> { s: String -> resultSet.getFloat(s) as T}
            Double::class -> { s: String -> resultSet.getDouble(s) as T}
            Long::class -> { s: String -> resultSet.getLong(s) as T}
            Short::class -> { s: String -> resultSet.getShort(s) as T}
            Byte::class -> { s: String -> resultSet.getByte(s) as T}
            BigDecimal::class -> {s: String -> resultSet.getBigDecimal(s) as T}
            else -> throw IllegalArgumentException("cannot find extract method for type: $kClass")
        }
    }

}