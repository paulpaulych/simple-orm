package simpleorm.core.jdbc

import org.slf4j.LoggerFactory
import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.sql.*
import java.sql.Array

fun PreparedStatement.setValues(values: List<Any?>): PreparedStatement{
    val log = LoggerFactory.getLogger(PreparedStatement::class.java)

    values.forEachIndexed{ i, v->
        val shiftedInd = i + 1
        if(v == null) {
            log.trace("setting null to pos $shiftedInd")
            this.setObject(shiftedInd, null)
            return@forEachIndexed
        }
        log.trace("setting param $v of ${v::class} to pos $shiftedInd")
        when(v::class){
            String::class -> this.setString(shiftedInd, v as String)
            Long::class -> this.setLong(shiftedInd, v as Long)
            Int::class -> this.setInt(shiftedInd, v as Int)
            Short::class -> this.setShort(shiftedInd, v as Short)
            BigDecimal::class -> this.setBigDecimal(shiftedInd, v as BigDecimal)
            Double::class -> this.setDouble(shiftedInd, v as Double)
            Float::class -> this.setFloat(shiftedInd, v as Float)
            Array::class -> this.setArray(shiftedInd, v as Array)
            Blob::class -> this.setBlob(shiftedInd, v as Blob)
            Clob::class -> this.setClob(shiftedInd, v as Clob)
            NClob::class -> this.setNClob(shiftedInd, v as NClob)
            //TODO: что с разными инпутстримами?
            InputStream::class -> this.setAsciiStream(shiftedInd, v as InputStream)
//            InputStream::class -> this.setBinaryStream(shiftedInd, v as InputStream)
            Reader::class -> this.setCharacterStream(shiftedInd, v as Reader)
            Boolean::class -> this.setBoolean(shiftedInd, v as Boolean)
            Byte::class -> this.setByte(shiftedInd, v as Byte)
            Date::class -> this.setDate(shiftedInd, v as Date)
            java.util.Date::class -> this.setTimestamp(shiftedInd, Timestamp.from((v as java.util.Date).toInstant()))
            //TODO: Дописать остальное
            else -> this.setObject(i, v)

        }
    }

    return this
}