package simpleorm.core.jdbc

import java.math.BigDecimal
import java.sql.ResultSet
import kotlin.reflect.KClass

internal fun <T: Any> byColumnGetter(kClass: KClass<T>, resultSet: ResultSet): (String) -> T {
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


internal fun <T: Any> byIndexGetter(kClass: KClass<T>, resultSet: ResultSet): (Int) -> T {
    return when(kClass) {
        Int::class -> { s: Int -> resultSet.getInt(s) as T}
        String::class -> { s: Int -> resultSet.getString(s) as T}
        Float::class -> { s: Int -> resultSet.getFloat(s) as T}
        Double::class -> { s: Int -> resultSet.getDouble(s) as T}
        Long::class -> { s: Int -> resultSet.getLong(s) as T}
        Short::class -> { s: Int -> resultSet.getShort(s) as T}
        Byte::class -> { s: Int -> resultSet.getByte(s) as T}
        BigDecimal::class -> {s: Int -> resultSet.getBigDecimal(s) as T}
        else -> throw IllegalArgumentException("cannot find extract method for type: $kClass")
    }
}

internal fun <T: Any> ResultSet.get(column: String, kClass: KClass<T>): T?{
    if(kClass.java.isPrimitive){
        return byColumnGetter(kClass, this).invoke(column)
    }
    return this.getObject(column, kClass.java)
}

internal fun <T: Any> ResultSet.get(columnIndex: Int, kClass: KClass<T>): T?{
    if(kClass.java.isPrimitive){
        return byIndexGetter(kClass, this).invoke(columnIndex)
    }
    return this.getObject(columnIndex, kClass.java)
}