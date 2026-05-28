package com.example.labcards.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.labcards.data.model.ExperimentCardEntity
import com.example.labcards.data.model.ExperimentTemplateEntity
import com.example.labcards.data.model.ExperimentTemplateSummary
import com.example.labcards.data.model.FlowNodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExperimentDao {
    @Query(
        """
        SELECT 
            experiment_templates.id AS id,
            experiment_templates.name AS name,
            experiment_templates.tags AS tags,
            experiment_templates.createdAt AS createdAt,
            experiment_templates.updatedAt AS updatedAt,
            CAST(COUNT(experiment_cards.id) AS INTEGER) AS cardCount
        FROM experiment_templates
        LEFT JOIN experiment_cards ON experiment_templates.id = experiment_cards.experimentTemplateId
        GROUP BY experiment_templates.id
        ORDER BY experiment_templates.updatedAt DESC
        """
    )
    fun getExperimentSummaries(): Flow<List<ExperimentTemplateSummary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperimentTemplate(template: ExperimentTemplateEntity): Long

    @Update
    suspend fun updateExperimentTemplate(template: ExperimentTemplateEntity)

    @Delete
    suspend fun deleteExperimentTemplate(template: ExperimentTemplateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlowNodes(nodes: List<FlowNodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperimentCards(cards: List<ExperimentCardEntity>): List<Long>

    @Query("DELETE FROM experiment_cards WHERE experimentTemplateId = :templateId")
    suspend fun deleteCardsForTemplate(templateId: Long)

    @Query("DELETE FROM flow_nodes WHERE experimentTemplateId = :templateId")
    suspend fun deleteFlowNodesForTemplate(templateId: Long)

    @Transaction
    @Query("SELECT * FROM experiment_templates WHERE id = :id")
    suspend fun getExperimentTemplateById(id: Long): ExperimentTemplateEntity?

    @Query("SELECT * FROM flow_nodes WHERE experimentTemplateId = :templateId ORDER BY orderIndex ASC")
    fun getFlowNodesForTemplate(templateId: Long): Flow<List<FlowNodeEntity>>

    @Query("SELECT * FROM flow_nodes WHERE experimentTemplateId = :templateId ORDER BY orderIndex ASC")
    suspend fun getFlowNodesSnapshot(templateId: Long): List<FlowNodeEntity>

    @Query("SELECT * FROM experiment_cards WHERE experimentTemplateId = :templateId ORDER BY orderIndex ASC")
    fun getCardsForTemplate(templateId: Long): Flow<List<ExperimentCardEntity>>

    @Query("SELECT * FROM experiment_cards WHERE experimentTemplateId = :templateId ORDER BY orderIndex ASC")
    suspend fun getCardsSnapshot(templateId: Long): List<ExperimentCardEntity>
}
