package com.example.labcards.data

import androidx.room.TypeConverter
import com.example.labcards.data.model.CardStyle
import com.example.labcards.data.model.FlowNodeType
import com.example.labcards.data.model.TimerMode
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String = Json.encodeToString(value)

    @TypeConverter
    fun toStringList(value: String): List<String> = Json.decodeFromString(value)

    @TypeConverter
    fun fromLongList(value: List<Long>): String = Json.encodeToString(value)

    @TypeConverter
    fun toLongList(value: String): List<Long> = Json.decodeFromString(value)

    @TypeConverter
    fun fromStringMap(value: Map<String, String>): String = Json.encodeToString(value)

    @TypeConverter
    fun toStringMap(value: String): Map<String, String> = Json.decodeFromString(value)

    @TypeConverter
    fun fromCardStyle(value: CardStyle): String = value.name

    @TypeConverter
    fun toCardStyle(value: String): CardStyle = CardStyle.valueOf(value)

    @TypeConverter
    fun fromTimerMode(value: TimerMode?): String? = value?.name

    @TypeConverter
    fun toTimerMode(value: String?): TimerMode? = value?.let(TimerMode::valueOf)

    @TypeConverter
    fun fromFlowNodeType(value: FlowNodeType): String = value.name

    @TypeConverter
    fun toFlowNodeType(value: String): FlowNodeType = FlowNodeType.valueOf(value)
}
