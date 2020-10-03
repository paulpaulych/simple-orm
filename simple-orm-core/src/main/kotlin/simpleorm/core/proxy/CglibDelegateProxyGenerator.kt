package simpleorm.core.proxy

import io.mockk.mockkClass
import net.sf.cglib.proxy.Enhancer
import paulpaulych.utils.LoggerDelegate
import simpleorm.core.delegate.IDelegateCreator
import simpleorm.core.schema.OrmSchema
import simpleorm.core.utils.immutableProperties
import simpleorm.core.utils.mutableProperties
import java.math.BigDecimal
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.reflect.full.primaryConstructor

/**
 * generates proxies for Entity classes in accordance with [OrmSchema] and [IDelegateCreator]
 * TODO(separate the responsibilities. Set this class not to know about ORM. Only delegated properties)
 */
class CglibDelegateProxyGenerator(
        private val ormSchema: OrmSchema,
        private val delegateCreator: IDelegateCreator
): ProxyGenerator {

    private val log by LoggerDelegate()

    override fun <T : Any> createProxyClass(kClass: KClass<T>, id: Any): T {
        val enhancer = Enhancer()
        val entityDescriptor = ormSchema.findEntityDescriptor(kClass)
        log.trace("creating proxy for $kClass")
        val interceptor = DelegatedPropertiesInterceptor(
                kClass,
                kClass.mutableProperties()
                        .filter { isConstructorParameter(it, kClass) }
                        .map {
                            it to delegateCreator.create(
                                    entityDescriptor,
                                    entityDescriptor.getPropertyDescriptor(it),
                                    id
                            )
                        }.toMap(),
                kClass.immutableProperties()
                        .filter { isConstructorParameter(it, kClass) }
                        .map {
                            it to delegateCreator.create(
                                    entityDescriptor,
                                    entityDescriptor.getPropertyDescriptor(it),
                                    id
                            )
                        }.toMap()
        )
        enhancer.setSuperclass(kClass.java)

        enhancer.setCallback(interceptor)
        val primaryConstructor = kClass.primaryConstructor
                ?: error("primary constructor not found for $kClass")

        val constructorArgTypes = primaryConstructor.parameters
                .map { it.type.classifier as KClass<*>}
                .map { it.java }
                .toTypedArray()

        val indexOfId = constructorArgTypes.indexOfFirst{it.name == entityDescriptor.idProperty.kProperty.name}

        val args = Array(constructorArgTypes.size){ind->
            val pName = primaryConstructor.parameters[ind].name
            val manyToOne = entityDescriptor.manyToOneProperties.values.find { it.kProperty.name == pName }
            when {
                ind == indexOfId -> id
                manyToOne != null -> mockkClass(manyToOne.kClass)
                else -> defaultValue(constructorArgTypes[ind].kotlin,
                        primaryConstructor.parameters[ind]!!.type.isMarkedNullable)
            }
        }

        val proxy = enhancer.create(
            constructorArgTypes,
            args
        )
        log.trace("proxy for $kClass created")
        return kClass.cast(proxy)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T: Any> defaultValue(kClass: KClass<T>, nullable: Boolean): T? {
        if (nullable) return null
        return when(kClass){
            Short::class -> Short.MIN_VALUE as T
            Byte::class -> Byte.MIN_VALUE as T
            Int::class -> Int.MIN_VALUE as T
            Long::class -> Long.MIN_VALUE as T
            Boolean::class -> false as T
            BigDecimal::class -> BigDecimal.ZERO as T
            String::class -> "DEFAULTVALUE. IF YOU SEE THIS PLEASE REPORT TO MAINTAINERS" as T
            List::class -> listOf<Any>() as T
            Date::class -> Date() as T
            else -> error("$kClass cannot be field of entity")
        }
    }

}