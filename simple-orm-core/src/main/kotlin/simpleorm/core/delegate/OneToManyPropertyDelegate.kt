package simpleorm.core.delegate

import simpleorm.core.filter.EqFilter
import simpleorm.core.findBy
import simpleorm.core.schema.property.OneToManyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

class OneToManyPropertyDelegate<T: Any>(
        private val oneToManyProperty: OneToManyProperty<T>,
        private val id: Any
): GenericDelegate<List<T>> {

    override fun getValue(thisRef: Any, property: KProperty<*>): List<T> {
        return oneToManyProperty.kClass.findBy(
                oneToManyProperty.kClass,
                listOf(
                    EqFilter(
                        oneToManyProperty.foreignKey as KProperty1<T, Any>,
                        id
                    )
                )
            )
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: List<T>?) {
        error("setting the value is prohibited")
    }

}