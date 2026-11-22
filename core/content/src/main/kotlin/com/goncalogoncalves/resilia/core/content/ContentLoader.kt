package com.goncalogoncalves.resilia.core.content

import com.goncalogoncalves.resilia.core.domain.model.Activity
import com.goncalogoncalves.resilia.core.domain.model.AlertsContent
import com.goncalogoncalves.resilia.core.domain.model.Contact
import com.goncalogoncalves.resilia.core.domain.model.ContentBundle
import com.goncalogoncalves.resilia.core.domain.model.ContentManifest
import com.goncalogoncalves.resilia.core.domain.model.FamilyPlanGuide
import com.goncalogoncalves.resilia.core.domain.model.HospitalsContent
import com.goncalogoncalves.resilia.core.domain.model.KitCatalog
import com.goncalogoncalves.resilia.core.domain.model.Material
import com.goncalogoncalves.resilia.core.domain.model.Scenario
import com.goncalogoncalves.resilia.core.domain.model.Source
import com.goncalogoncalves.resilia.core.domain.model.SupportContent
import com.goncalogoncalves.resilia.core.domain.model.ToolGuide
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Reads a file from the content root by relative path (e.g. "scenarios/power-outage.json"). */
fun interface ContentFileReader {
    fun read(relativePath: String): String
}

@Serializable
internal data class SourcesFile(val sources: List<Source>)

@Serializable
internal data class ContactsFile(val contacts: List<Contact>)

@Serializable
internal data class ActivitiesFile(val activities: List<Activity>)

@Serializable
internal data class MaterialsFile(val materials: List<Material>)

/**
 * Parses the content folder into a [ContentBundle]. Pure Kotlin: used by the app
 * (Android assets), by the CI validator (file system) and by tests.
 */
class ContentLoader(private val reader: ContentFileReader) {

    fun load(): ContentBundle {
        val manifest = json.decodeFromString<ContentManifest>(reader.read(MANIFEST))
        val sources = json.decodeFromString<SourcesFile>(reader.read(manifest.sources)).sources
        val scenarios = manifest.scenarios.map { path ->
            try {
                json.decodeFromString<Scenario>(reader.read(path))
            } catch (e: Exception) {
                throw ContentParseException("Invalid scenario file '$path': ${e.message}", e)
            }
        }
        val contacts = try {
            json.decodeFromString<ContactsFile>(reader.read(manifest.contacts)).contacts
        } catch (e: Exception) {
            throw ContentParseException("Invalid contacts file '${manifest.contacts}': ${e.message}", e)
        }
        val kit = try {
            json.decodeFromString<KitCatalog>(reader.read(manifest.kit))
        } catch (e: Exception) {
            throw ContentParseException("Invalid kit file '${manifest.kit}': ${e.message}", e)
        }
        val plan = try {
            json.decodeFromString<FamilyPlanGuide>(reader.read(manifest.plan))
        } catch (e: Exception) {
            throw ContentParseException("Invalid plan file '${manifest.plan}': ${e.message}", e)
        }
        val battery = try {
            json.decodeFromString<ToolGuide>(reader.read(manifest.battery))
        } catch (e: Exception) {
            throw ContentParseException("Invalid battery guide '${manifest.battery}': ${e.message}", e)
        }
        val activities = parse("activities", manifest.activities) { json.decodeFromString<ActivitiesFile>(it).activities }
        val materials = parse("materials", manifest.materials) { json.decodeFromString<MaterialsFile>(it).materials }
        val support = parse("support", manifest.support) { json.decodeFromString<SupportContent>(it) }
        val alerts = parse("alerts", manifest.alerts) { json.decodeFromString<AlertsContent>(it) }
        val hospitals = parse("hospitals", manifest.hospitals) { json.decodeFromString<HospitalsContent>(it) }
        return ContentBundle(manifest, sources, scenarios, contacts, kit, plan, battery, activities, materials, support, alerts, hospitals)
    }

    private fun <T> parse(what: String, path: String, decode: (String) -> T): T = try {
        decode(reader.read(path))
    } catch (e: Exception) {
        throw ContentParseException("Invalid $what file '$path': ${e.message}", e)
    }

    companion object {
        const val MANIFEST = "manifest.json"

        /** Strict on purpose: unknown keys are probably typos in contributed content. */
        val json = Json {
            ignoreUnknownKeys = false
            isLenient = false
        }
    }
}

class ContentParseException(message: String, cause: Throwable? = null) : Exception(message, cause)
