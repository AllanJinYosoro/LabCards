package com.example.labcards.data

import androidx.room.withTransaction
import com.example.labcards.data.model.CardTemplateEntity
import com.example.labcards.data.model.ExperimentCardEntity
import com.example.labcards.data.model.ExperimentTemplateEntity
import com.example.labcards.data.model.ExperimentTemplateSummary
import com.example.labcards.data.model.FlowNodeEntity
import com.example.labcards.data.model.FlowNodeType
import kotlinx.coroutines.flow.Flow

data class ExperimentData(
    val template: ExperimentTemplateEntity,
    val cards: List<ExperimentCardEntity>
)

class LabRepository(private val database: AppDatabase) {
    private val experimentDao = database.experimentDao()
    private val cardDao = database.cardDao()

    val experimentSummaries: Flow<List<ExperimentTemplateSummary>> =
        experimentDao.getExperimentSummaries()

    val cardTemplates: Flow<List<CardTemplateEntity>> =
        cardDao.getAllTemplates()

    suspend fun getCardTemplate(id: Long): CardTemplateEntity? =
        cardDao.getTemplateById(id)

    suspend fun saveCardTemplate(template: CardTemplateEntity): Long =
        cardDao.insertTemplate(template)

    suspend fun getExperiment(id: Long): ExperimentData? {
        val template = experimentDao.getExperimentTemplateById(id) ?: return null
        return ExperimentData(
            template = template,
            cards = experimentDao.getCardsSnapshot(id)
        )
    }

    fun observeCards(templateId: Long): Flow<List<ExperimentCardEntity>> =
        experimentDao.getCardsForTemplate(templateId)

    suspend fun saveExperiment(
        templateId: Long,
        name: String,
        tags: String,
        cards: List<ExperimentCardEntity>,
        saveAsNew: Boolean = false
    ): Long {
        val now = System.currentTimeMillis()
        return database.withTransaction {
            val targetId = if (templateId != 0L && !saveAsNew) {
                val old = experimentDao.getExperimentTemplateById(templateId)
                experimentDao.updateExperimentTemplate(
                    ExperimentTemplateEntity(
                        id = templateId,
                        name = name.trim(),
                        tags = tags.trim(),
                        createdAt = old?.createdAt ?: now,
                        updatedAt = now
                    )
                )
                experimentDao.deleteCardsForTemplate(templateId)
                experimentDao.deleteFlowNodesForTemplate(templateId)
                templateId
            } else {
                experimentDao.insertExperimentTemplate(
                    ExperimentTemplateEntity(
                        name = if (saveAsNew && templateId != 0L) "${name.trim()} 副本" else name.trim(),
                        tags = tags.trim(),
                        createdAt = now,
                        updatedAt = now
                    )
                )
            }

            val insertedCardIds = experimentDao.insertExperimentCards(
                cards.mapIndexed { index, card ->
                    card.copy(
                        id = 0,
                        experimentTemplateId = targetId,
                        orderIndex = index,
                        updatedAt = now
                    )
                }
            )

            experimentDao.insertFlowNodes(
                insertedCardIds.mapIndexed { index, cardId ->
                    FlowNodeEntity(
                        experimentTemplateId = targetId,
                        nodeType = FlowNodeType.CARD,
                        cardId = cardId,
                        orderIndex = index
                    )
                }
            )
            targetId
        }
    }

    suspend fun copyExperiment(templateId: Long): Long? {
        val data = getExperiment(templateId) ?: return null
        return saveExperiment(
            templateId = 0,
            name = "${data.template.name} 副本",
            tags = data.template.tags,
            cards = data.cards,
            saveAsNew = false
        )
    }

    suspend fun deleteExperiment(templateId: Long) {
        database.withTransaction {
            val template = experimentDao.getExperimentTemplateById(templateId) ?: return@withTransaction
            experimentDao.deleteFlowNodesForTemplate(templateId)
            experimentDao.deleteCardsForTemplate(templateId)
            experimentDao.deleteExperimentTemplate(template)
        }
    }
}
