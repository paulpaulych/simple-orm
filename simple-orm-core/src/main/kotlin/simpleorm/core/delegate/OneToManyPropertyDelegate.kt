package simpleorm.core.delegate

import simpleorm.core.filter.EqFilter
import simpleorm.core.filter.FetchFilter
import simpleorm.core.findRepo
import simpleorm.core.schema.property.OneToManyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

class OneToManyPropertyDelegate<T: Any>(
        private val oneToManyProperty: OneToManyProperty<T>,
        private val id: Any
): GenericDelegate<List<T>> {

    override fun getValue(thisRef: Any, property: KProperty<*>): List<T> {
        return oneToManyProperty.kClass.findBy(
                oneToManyProperty.kClass,
                EqFilter(
                    oneToManyProperty.foreignKey,
                    id
                )
            )
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: List<T>?) {
        error("setting the value is prohibited")
    }

}

internal fun <T: Any> KClass<T>.findBy(kClass: KClass<T>, filter: FetchFilter): List<T>{
    return findRepo(kClass).findBy(filter)
}