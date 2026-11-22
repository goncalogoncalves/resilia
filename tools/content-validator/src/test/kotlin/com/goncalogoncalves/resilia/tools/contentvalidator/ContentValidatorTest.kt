package com.goncalogoncalves.resilia.tools.contentvalidator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ContentValidatorTest {

    @get:Rule val tmp = TemporaryFolder()

    private val realContent = File(requireNotNull(System.getProperty("resilia.contentDir")))

    @Test
    fun `bundled content is valid`() {
        assertEquals(emptyList<String>(), ContentValidator(realContent).validate())
    }

    /** Copy of the real content with one scenario file replaced. */
    private fun contentWith(scenarioJson: String): File {
        val root = tmp.newFolder("content")
        realContent.copyRecursively(root, overwrite = true)
        File(root, "scenarios/power-outage.json").writeText(scenarioJson)
        return root
    }

    @Test
    fun `missing english translation fails schema`() {
        val original = File(realContent, "scenarios/power-outage.json").readText()
        val broken = original.replaceFirst(
            "\"en\": \"Power outage\"",
            "\"es\": \"Apagón\"",
        )
        val errors = ContentValidator(contentWith(broken)).validate()
        assertTrue(errors.joinToString("\n"), errors.any { "power-outage.json" in it })
    }

    @Test
    fun `unlisted scenario file is reported`() {
        val root = tmp.newFolder("content2")
        realContent.copyRecursively(root, overwrite = true)
        File(root, "scenarios/orphan.json").writeText("{}")
        val errors = ContentValidator(root).validate()
        assertTrue(errors.any { "orphan.json: not listed" in it })
    }

    @Test
    fun `invalid json is reported`() {
        val errors = ContentValidator(contentWith("{ not json")).validate()
        assertTrue(errors.any { "invalid JSON" in it })
    }

    @Test
    fun `contact without source fails`() {
        val root = tmp.newFolder("content3")
        realContent.copyRecursively(root, overwrite = true)
        val file = File(root, "contacts/national.json")
        file.writeText(file.readText().replace("\"sourceIds\": [\"inem-ciav\"]", "\"sourceIds\": []"))
        val errors = ContentValidator(root).validate()
        assertTrue(errors.joinToString("\n"), errors.any { "national.json" in it })
    }
}
