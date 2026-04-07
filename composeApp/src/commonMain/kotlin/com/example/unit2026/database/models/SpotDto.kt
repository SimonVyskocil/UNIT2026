package com.example.unit2026.database.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.listSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class SpotDto(
    val id: Long? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    val name: String,
    val description: String? = null,

    val certified: Boolean = false,

    val coordinates: String,

    @SerialName("opening_hours")
    val openingHours: String? = null,

    @SerialName("power_outlet")
    val powerOutlet: Boolean = false,

    val noise: Int,
    val comfort: Int,
    val refreshments: Int,

    @SerialName("photos")
    @Serializable(with = ImageUrlsSerializer::class)
    val imageUrls: List<String> = emptyList()
)

private object ImageUrlsSerializer : KSerializer<List<String>> {
    override val descriptor: SerialDescriptor = listSerialDescriptor<String>()

    override fun serialize(encoder: Encoder, value: List<String>) {
        encoder.encodeSerializableValue(ListSerializer(String.serializer()), value)
    }

    override fun deserialize(decoder: Decoder): List<String> {
        val jsonDecoder = decoder as? JsonDecoder ?: return emptyList()
        val element = jsonDecoder.decodeJsonElement()
        return parseImageUrls(element)
    }

    private fun parseImageUrls(element: JsonElement): List<String> {
        return when (element) {
            is JsonArray -> element.mapNotNull { it.jsonPrimitive.contentOrNull?.trim() }.filter { it.isNotEmpty() }
            is JsonPrimitive -> parsePrimitive(element)
            else -> emptyList()
        }
    }

    private fun parsePrimitive(primitive: JsonPrimitive): List<String> {
        val value = primitive.contentOrNull?.trim().orEmpty()
        if (value.isEmpty() || value == "null") return emptyList()

        if (value.startsWith("[")) {
            return runCatching {
                Json.parseToJsonElement(value)
            }.getOrNull()?.let { parseImageUrls(it) } ?: emptyList()
        }

        return listOf(value)
    }
}
