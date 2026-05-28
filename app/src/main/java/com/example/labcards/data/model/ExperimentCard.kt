package com.example.labcards.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "experiment_cards")
@Serializable
data class ExperimentCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val experimentTemplateId: Long,
    val cardTemplateId: Long?,
    val orderIndex: Int,
    val contentBlocksJson: String,
    val style: CardStyle = CardStyle.NORMAL,
    val hasTimer: Boolean = false,
    val timerMode: TimerMode? = null,
    val fixedTimerDurationSeconds: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
