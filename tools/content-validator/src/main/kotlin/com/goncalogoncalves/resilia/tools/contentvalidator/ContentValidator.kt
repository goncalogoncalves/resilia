package com.goncalogoncalves.resilia.tools.contentvalidator

import com.fasterxml.jackson.databind.ObjectMapper
import com.goncalogoncalves.resilia.core.content.ContentLoader
import com.goncalogoncalves.resilia.core.content.ContentRules
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import java.io.File

/**
 * Validates a content folder in two passes:
 * 1. every file against its JSON Schema (structure, required languages);
 * 2. the parsed bundle against [ContentRules] (references, official sources, review metadata).
 */
class ContentValidator(private val root: File) {

    private val mapper = ObjectMapper()
    private val schemaDir = File(root, "schema")

    private val factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012) { builder ->
        builder.schemaMappers { mappers ->
            mappers.mapPrefix(SCHEMA_BASE, schemaDir.toURI().toString())
        }
    }

    private fun schema(name: String): JsonSchema =
        factory.getSchema(java.net.URI.create(SCHEMA_BASE + name))

    fun validate(): List<String> {
        val errors = mutableListOf<String>()

        val manifestFile = File(root, ContentLoader.MANIFEST)
        if (!manifestFile.exists()) return listOf("manifest.json not found in $root")
        errors += validateFile(manifestFile, "manifest.schema.json")

        val manifest = mapper.readTree(manifestFile)
        manifest.path("sources").asText().takeIf { it.isNotEmpty() }?.let {
            errors += validateFile(File(root, it), "sources.schema.json")
        }
        manifest.path("contacts").asText().takeIf { it.isNotEmpty() }?.let {
            errors += validateFile(File(root, it), "contacts.schema.json")
        }
        manifest.path("kit").asText().takeIf { it.isNotEmpty() }?.let {
            errors += validateFile(File(root, it), "kit.schema.json")
        }
        manifest.path("plan").asText().takeIf { it.isNotEmpty() }?.let {
            errors += validateFile(File(root, it), "plan.schema.json")
        }
        manifest.path("battery").asText().takeIf { it.isNotEmpty() }?.let {
            errors += validateFile(File(root, it), "toolguide.schema.json")
        }
        for ((key, schemaName) in listOf(
            "activities" to "activities.schema.json",
            "materials" to "materials.schema.json",
            "support" to "support.schema.json",
            "alerts" to "alerts.schema.json",
            "hospitals" to "hospitals.schema.json",
        )) {
            manifest.path(key).asText().takeIf { it.isNotEmpty() }?.let {
                errors += validateFile(File(root, it), schemaName)
            }
        }
        manifest.path("scenarios").forEach {
            errors += validateFile(File(root, it.asText()), "scenario.schema.json")
        }

        // Files present on disk but not listed in the manifest are almost always a mistake.
        val listed = manifest.path("scenarios").map { File(root, it.asText()).canonicalFile }.toSet()
        File(root, "scenarios").listFiles { f -> f.extension == "json" }?.forEach {
            if (it.canonicalFile !in listed) errors += "scenarios/${it.name}: not listed in manifest.json"
        }

        if (errors.isNotEmpty()) return errors // don't parse structurally broken files

        return try {
            ContentRules.check(ContentLoader { File(root, it).readText() }.load())
        } catch (e: Exception) {
            listOf("parse error: ${e.message}")
        }
    }

    private fun validateFile(file: File, schemaName: String): List<String> {
        val rel = file.relativeTo(root).invariantSeparatorsPath
        if (!file.exists()) return listOf("$rel: file not found")
        val node = try {
            mapper.readTree(file)
        } catch (e: Exception) {
            return listOf("$rel: invalid JSON (${e.message})")
        }
        return schema(schemaName).validate(node).map { "$rel: ${it.message}" }
    }

    companion object {
        const val SCHEMA_BASE = "https://resilia.local/schema/"
    }
}
