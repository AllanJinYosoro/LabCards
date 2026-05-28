package com.example.labcards.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.labcards.data.model.CardTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM card_templates ORDER BY updatedAt DESC")
    fun getAllTemplates(): Flow<List<CardTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: CardTemplateEntity): Long

    @Update
    suspend fun updateTemplate(template: CardTemplateEntity)

    @Delete
    suspend fun deleteTemplate(template: CardTemplateEntity)

    @Query("SELECT * FROM card_templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): CardTemplateEntity?
}
