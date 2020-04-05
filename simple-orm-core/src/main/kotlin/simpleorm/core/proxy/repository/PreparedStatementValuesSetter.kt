package simpleorm.core.proxy.repository

import paulpaulych.utils.LoggerDelegate
import simpleorm.core.jdbc.PreparedStatementSetter
import java.math.BigDecimal
import java.sql.PreparedStatement
import kotlin.reflect.KClass

class PreparedStatementValuesSetter(
        val values: List<Any>
): PreparedStatementSetter {

    private val log by LoggerDelegate()

    override fun set(ps: PreparedStatement): PreparedStatement {
        values.forEachIndexed{i, v->
            set(v::class, ps, i+1, v)
        }
        return ps
    }

    fun <T: Any> set(kClass: KClass<T>, ps: PreparedStatement, i: Int, value: Any){
        log.trace("setting param $value of $kClass to pos $i")
        return when(kClass){
            String::class -> ps.setString(i, value as String)
            Long::class -> ps.setLong(i, value as Long)
            Int::class -> ps.setInt(i, value as Int)
            Short::class -> ps.setShort(i, value as Short)
            BigDecimal::class -> ps.setBigDecimal(i, value as BigDecimal)
            Double::class -> ps.setDouble(i, value as Double)
            Float::class -> ps.setFloat(i, value as Float)
            else -> error("cannot persist $kClass: setter not found")
        }
    }
}