package tradefirm.simpleorm

import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import java.io.File
import java.io.FileReader


class YamlParserTest: FunSpec({

    test("asfd"){
        val yamlParser = SchemaParser()
        val file = File(javaClass.classLoader.getResource("schema.yml").file)
        val readText = FileReader(file).readText()
        val parsed = yamlParser.load(readText)
        parsed shouldBe EntityDescriptorList(
            mapOf(
                "tradefirm.domain.model.ProductKt" to EntityDescriptor(
                    "product",
                    mapOf(
                        "id" to "product_id",
                        "name" to "product_name"
                    )
                ),
                "tradefirm.domain.model.SupplierKt" to EntityDescriptor(
                    "supplier",
                    mapOf(
                        "id" to "supplier_id",
                        "name" to "company_name"
                    )
                )
            )
        )
    }
})