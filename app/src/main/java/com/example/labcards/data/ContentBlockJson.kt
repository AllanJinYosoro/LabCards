package com.example.labcards.data

import com.example.labcards.data.model.CardContentBlock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ContentBlockJson {
    val json: Json = Json {
        classDiscriminator = "type"
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(blocks: List<CardContentBlock>): String = json.encodeToString(blocks)

    fun decode(value: String): List<CardContentBlock> {
        if (value.isBlank()) return emptyList()
        return json.decodeFromString(value)
    }
}
