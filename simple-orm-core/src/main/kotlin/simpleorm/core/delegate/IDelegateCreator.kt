package simpleorm.core.delegate

import simpleorm.core.schema.EntityDescriptor
import simpleorm.core.schema.property.PropertyDescriptor


/**
 *
 */
interface IDelegateCreator{

    fun <T : Any, R : Any> create(ed: EntityDescriptor<R>, pd: PropertyDescriptor<T>, initialValue: Any): GenericDelegate<T>

}