package com.example.labcards.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "experiment_templates")
@Serializable
data class ExperimentTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val tags: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ExperimentTemplateSummary(
    val id: Long,
    val name: String,
    val tags: String,
    val createdAt: Long,
    val updatedAt: Long,
    val cardCount: Int
)
