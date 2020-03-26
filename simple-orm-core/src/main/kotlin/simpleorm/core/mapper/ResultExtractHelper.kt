package simpleorm.core.mapper

import java.sql.ResultSet
import kotlin.reflect.KClass

interface ResultExtractHelper{
    fun <T: Any> getter(kClass: KClass<T>, resultSet: ResultSet): (String) -> T
}

