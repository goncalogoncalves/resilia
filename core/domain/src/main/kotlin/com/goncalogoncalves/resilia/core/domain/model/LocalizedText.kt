package com.goncalogoncalves.resilia.core.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** Content languages. Tags match the keys used in the content JSON. */
enum class ContentLanguage(val tag: String) {
    PT_PT("pt-PT"),
    EN("en");

    companion object {
        val DEFAULT = PT_PT

        /** Maps a device/system language (ISO 639 code, e.g. "pt", "en") to a content language. */
        fun fromSystemLanguage(language: String): ContentLanguage =
            if (language.lowercase().startsWith("pt")) PT_PT else EN
    }
}

/**
 * Text in every supported content language, e.g. `{"pt-PT": "...", "en": "..."}`.
 * The content validator guarantees both keys exist and are non-blank.
 */
@Serializable(with = LocalizedTextSerializer::class)
data class LocalizedText(val values: Map<String, String>) {

    fun resolve(language: ContentLanguage): String =
        values[language.tag]?.takeIf { it.isNotBlank() }
            ?: values[ContentLanguage.DEFAULT.tag]
            ?: values.values.firstOrNull()
            ?: ""

    fun missingLanguages(): List<ContentLanguage> =
        ContentLanguage.entries.filter { values[it.tag].isNullOrBlank() }

    companion object {
        fun of(pt: String, en: String) = LocalizedText(
            mapOf(ContentLanguage.PT_PT.tag to pt, ContentLanguage.EN.tag to en),
        )
    }
}

internal object LocalizedTextSerializer : KSerializer<LocalizedText> {
    private val delegate = MapSerializer(String.serializer(), String.serializer())
    override val descriptor: SerialDescriptor = delegate.descriptor
    override fun serialize(encoder: Encoder, value: LocalizedText) =
        delegate.serialize(encoder, value.values)

    override fun deserialize(decoder: Decoder) = LocalizedText(delegate.deserialize(decoder))
}
