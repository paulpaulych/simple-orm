
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import paulpaulych.utils.ResourceLoader
import simpleorm.core.schema.ast.RawEntityDescriptor
import simpleorm.core.schema.ast.RawOrmSchema
import simpleorm.core.schema.ast.RawFieldDescriptor
import simpleorm.core.schema.SchemaParser
import simpleorm.test.ExampleEntity

class RawOrmSchemaTest: FunSpec( ){

    init {

        test("rawSchemaTest"){

            val loadText = ResourceLoader.loadText("test-schema.yml")
            SchemaParser(loadText).parse() shouldBe RawOrmSchema(
                    mapOf(
                            "${ExampleEntity::class.qualifiedName}" to RawEntityDescriptor(
                                    "example",
                                    mapOf(
                                            "longValue" to RawFieldDescriptor(true, "long_value"),
                                            "stringValue" to RawFieldDescriptor(column = "string_value")
                                    )
                            )
                    )
            )

        }

    }

}