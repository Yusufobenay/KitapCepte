package com.kitapcepte.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class WorkDto(
    val description: JsonElement? = null,
    val title: String? = null
) {
    val descriptionText: String?
        get() = when (val elem = description) {
            is JsonPrimitive -> elem.content
            is JsonObject -> elem["value"]?.jsonPrimitive?.content
            else -> null
        }
}
