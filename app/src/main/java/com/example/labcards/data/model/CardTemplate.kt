package com.example.labcards.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity(tableName = "card_templates")
@Serializable
data class CardTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val contentBlocksJson: String,
    val style: CardStyle = CardStyle.NORMAL,
    val hasTimer: Boolean = false,
    val timerMode: TimerMode? = null,
    val fixedTimerDurationSeconds: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class TimerMode {
    BOUND_TO_TIME_INPUT,
    FIXED
}

enum class CardStyle {
    NORMAL, DANGER, WARNING, INFO
}

enum class TimerState {
    IDLE, RUNNING, PAUSED, FINISHED
}

enum class TimeInputUnit(val label: String, val secondsMultiplier: Long) {
    SECONDS("s", 1),
    MINUTES("min", 60),
    HOURS("h", 3600)
}

@Serializable
sealed class CardContentBlock {
    abstract val id: String

    @Serializable
    @SerialName("TEXT")
    data class TextBlock(
        override val id: String,
        val text: String
    ) : CardContentBlock()

    @Serializable
    @SerialName("NUMBER_INPUT")
    data class NumberInputBlock(
        override val id: String,
        val value: String,
        val unit: String? = null
    ) : CardContentBlock()

    @Serializable
    @SerialName("TIME_INPUT")
    data class TimeInputBlock(
        override val id: String,
        val valueSeconds: Long,
        val unit: TimeInputUnit = TimeInputUnit.SECONDS
    ) : CardContentBlock()
}
