
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import paulpaulych.utils.ResourceLoader
import simpleorm.core.schema.EntityDescriptor
import simpleorm.core.schema.EntityDescriptorList
import simpleorm.core.schema.FieldDescriptor
import simpleorm.core.schema.SchemaParser
import simpleorm.test.ExampleEntity

class SchemaLoaderTest: FunSpec( ){

    init {

        test("schemaParserTest"){

            val loadText = ResourceLoader.loadText("test-schema.yml")
            SchemaParser(loadText).parse() shouldBe EntityDescriptorList(
                    mapOf(
                        "${ExampleEntity::class.qualifiedName}" to  EntityDescriptor(
                            "example",
                            mapOf(
                                "longValue" to  FieldDescriptor(true, "long_value"),
                                "stringValue" to  FieldDescriptor(column = "string_value")
                            )
                        )
                    )
            )

        }
    }

}