package simpleorm.core.schema.naming

fun toSnakeCase(s: String): String{
    return s.fold(""){ acc, c ->
        if(!acc.isEmpty() && c.isUpperCase()){
            acc + "_" + c.toLowerCase()
        }else{
            acc + c.toString()
        }
    }
}