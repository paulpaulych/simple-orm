package simpleorm.core.mapper

import java.sql.ResultSet
import kotlin.reflect.KClassifier


fun ResultSet.getterByType(kClass: KClassifier): (String)->Any{
    return when(kClass){
        Int::class -> { s: String -> this.getInt(s)}
        String::class -> { s: String -> this.getString(s)}
        Long::class -> { s: String -> this.getInt(s)}
        Short::class -> { s: String -> this.getInt(s)}
        List::class -> {s: String -> listOf<Any>()}
        else -> error("cannot find extract for type: $kClass")
    }
}
