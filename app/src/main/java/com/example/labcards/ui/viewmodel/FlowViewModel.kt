package com.example.labcards.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.labcards.data.AppDatabase
import com.example.labcards.data.ContentBlockJson
import com.example.labcards.data.LabRepository
import com.example.labcards.data.model.CardContentBlock
import com.example.labcards.data.model.CardStyle
import com.example.labcards.data.model.CardTemplateEntity
import com.example.labcards.data.model.ExperimentCardEntity
import com.example.labcards.data.model.ExperimentTemplateSummary
import com.example.labcards.data.model.TimeInputUnit
import com.example.labcards.data.model.TimerMode
import com.example.labcards.domain.LabValidators
import com.example.labcards.domain.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class CardDraft(
    val id: Long = 0,
    val experimentTemplateId: Long = 0,
    val cardTemplateId: Long? = null,
    val orderIndex: Int = 0,
    val blocks: List<CardContentBlock> = listOf(
        CardContentBlock.TextBlock(newBlockId(), "实验步骤")
    ),
    val style: CardStyle = CardStyle.NORMAL,
    val hasTimer: Boolean = false,
    val timerMode: TimerMode? = null,
    val fixedTimerDurationSeconds: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun validation(): ValidationResult =
        LabValidators.validateCard(blocks, hasTimer, timerMode, fixedTimerDurationSeconds)
}

data class ExperimentEditorState(
    val templateId: Long = 0,
    val name: String = "",
    val tags: String = "",
    val cards: List<CardDraft> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastSavedId: Long? = null
)

enum class CardEditorMode {
    FLOW_CARD,
    TEMPLATE
}

data class CardEditorState(
    val index: Int? = null,
    val templateId: Long? = null,
    val mode: CardEditorMode = CardEditorMode.FLOW_CARD,
    val draft: CardDraft = CardDraft(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FlowViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LabRepository(AppDatabase.getDatabase(application))

    val experimentSummaries: StateFlow<List<ExperimentTemplateSummary>> =
        repository.experimentSummaries.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val cardTemplates: StateFlow<List<CardTemplateEntity>> =
        repository.cardTemplates.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _editorState = MutableStateFlow(ExperimentEditorState())
    val editorState: StateFlow<ExperimentEditorState> = _editorState.asStateFlow()

    private val _cardEditorState = MutableStateFlow(CardEditorState())
    val cardEditorState: StateFlow<CardEditorState> = _cardEditorState.asStateFlow()

    fun openEditor(templateId: Long) {
        val current = _editorState.value
        if (templateId == 0L && current.templateId == 0L && !current.isLoading) {
            return
        }
        if (templateId != 0L && current.templateId == templateId && !current.isLoading) {
            return
        }

        viewModelScope.launch {
            _editorState.value = ExperimentEditorState(templateId = templateId, isLoading = true)
            if (templateId == 0L) {
                _editorState.value = ExperimentEditorState()
                return@launch
            }

            val data = repository.getExperiment(templateId)
            _editorState.value = if (data == null) {
                ExperimentEditorState(error = "未找到实验流程")
            } else {
                ExperimentEditorState(
                    templateId = data.template.id,
                    name = data.template.name,
                    tags = data.template.tags,
                    cards = data.cards.map { it.toDraft() }
                )
            }
        }
    }

    fun updateExperimentName(value: String) {
        _editorState.update { it.copy(name = value, error = null) }
    }

    fun updateExperimentTags(value: String) {
        _editorState.update { it.copy(tags = value, error = null) }
    }

    fun startCardEdit(index: Int?) {
        val draft = index?.let { _editorState.value.cards.getOrNull(it) }
            ?: CardDraft(orderIndex = _editorState.value.cards.size)
        _cardEditorState.value = CardEditorState(
            index = index,
            mode = CardEditorMode.FLOW_CARD,
            draft = draft,
            error = null
        )
    }

    fun startTemplateCreate() {
        _cardEditorState.value = CardEditorState(
            templateId = null,
            mode = CardEditorMode.TEMPLATE,
            draft = CardDraft(),
            error = null
        )
    }

    fun startTemplateEdit(templateId: Long) {
        _cardEditorState.value = CardEditorState(
            templateId = templateId,
            mode = CardEditorMode.TEMPLATE,
            draft = CardDraft(),
            isLoading = true,
            error = null
        )
        viewModelScope.launch {
            val template = repository.getCardTemplate(templateId)
            if (template == null) {
                _cardEditorState.update { it.copy(isLoading = false, error = "未找到卡片模板") }
                return@launch
            }
            _cardEditorState.value = CardEditorState(
                templateId = template.id,
                mode = CardEditorMode.TEMPLATE,
                draft = template.toDraft(),
                isLoading = false,
                error = null
            )
        }
    }

    fun addTextBlock() = appendBlock(CardContentBlock.TextBlock(newBlockId(), ""))

    fun addNumberBlock() = appendBlock(CardContentBlock.NumberInputBlock(newBlockId(), "0", ""))

    fun addTimeBlock() {
        val state = _cardEditorState.value
        if (state.draft.blocks.any { it is CardContentBlock.TimeInputBlock }) {
            _cardEditorState.value = state.copy(error = "一张卡片最多只能有一个时间输入框")
            return
        }
        appendBlock(CardContentBlock.TimeInputBlock(newBlockId(), 60, TimeInputUnit.SECONDS))
        _cardEditorState.update {
            it.copy(
                draft = it.draft.copy(
                    hasTimer = true,
                    timerMode = TimerMode.BOUND_TO_TIME_INPUT,
                    fixedTimerDurationSeconds = null
                ),
                error = null
            )
        }
    }

    private fun appendBlock(block: CardContentBlock) {
        _cardEditorState.update {
            it.copy(draft = it.draft.copy(blocks = it.draft.blocks + block), error = null)
        }
    }

    fun updateBlock(blockId: String, replacement: CardContentBlock) {
        _cardEditorState.update { state ->
            state.copy(
                draft = state.draft.copy(
                    blocks = state.draft.blocks.map { if (it.id == blockId) replacement else it }
                ),
                error = null
            )
        }
    }

    fun removeBlock(blockId: String) {
        _cardEditorState.update { state ->
            val newBlocks = state.draft.blocks.filterNot { it.id == blockId }
            val hasTimeInput = newBlocks.any { it is CardContentBlock.TimeInputBlock }
            state.copy(
                draft = state.draft.copy(
                    blocks = newBlocks,
                    hasTimer = if (hasTimeInput) true else state.draft.hasTimer && state.draft.timerMode == TimerMode.FIXED,
                    timerMode = if (hasTimeInput) TimerMode.BOUND_TO_TIME_INPUT else state.draft.timerMode?.takeIf { it == TimerMode.FIXED },
                    fixedTimerDurationSeconds = if (hasTimeInput) null else state.draft.fixedTimerDurationSeconds
                ),
                error = null
            )
        }
    }

    fun updateCardStyle(style: CardStyle) {
        _cardEditorState.update { it.copy(draft = it.draft.copy(style = style), error = null) }
    }

    fun setFixedTimerEnabled(enabled: Boolean) {
        _cardEditorState.update { state ->
            val hasTimeInput = state.draft.blocks.any { it is CardContentBlock.TimeInputBlock }
            val draft = when {
                enabled && hasTimeInput -> state.draft.copy(
                    hasTimer = true,
                    timerMode = TimerMode.BOUND_TO_TIME_INPUT,
                    fixedTimerDurationSeconds = null
                )
                enabled -> state.draft.copy(
                    hasTimer = true,
                    timerMode = TimerMode.FIXED,
                    fixedTimerDurationSeconds = state.draft.fixedTimerDurationSeconds ?: 60
                )
                hasTimeInput -> state.draft
                else -> state.draft.copy(hasTimer = false, timerMode = null, fixedTimerDurationSeconds = null)
            }
            state.copy(draft = draft, error = null)
        }
    }

    fun updateFixedTimer(secondsText: String) {
        _cardEditorState.update {
            it.copy(draft = it.draft.copy(fixedTimerDurationSeconds = secondsText.toLongOrNull()), error = null)
        }
    }

    fun commitCardDraft(): Boolean {
        val state = _cardEditorState.value
        if (state.mode == CardEditorMode.TEMPLATE) {
            commitTemplateDraft()
            return state.draft.validation().isValid
        }

        val validation = state.draft.validation()
        if (!validation.isValid) {
            _cardEditorState.value = state.copy(error = validation.message)
            return false
        }

        val current = _editorState.value.cards
        val updated = if (state.index == null) {
            current + state.draft.copy(orderIndex = current.size)
        } else {
            current.mapIndexed { index, card ->
                if (index == state.index) state.draft.copy(orderIndex = index) else card
            }
        }.mapIndexed { index, card -> card.copy(orderIndex = index) }

        _editorState.update { it.copy(cards = updated, error = null) }
        _cardEditorState.update { it.copy(error = null) }
        saveDraftAsReusableTemplate(state.draft)
        return true
    }

    fun commitCardEditor(onSaved: () -> Unit = {}) {
        val state = _cardEditorState.value
        if (state.mode == CardEditorMode.TEMPLATE) {
            commitTemplateDraft(onSaved)
            return
        }
        if (commitCardDraft()) {
            onSaved()
        }
    }

    private fun commitTemplateDraft(onSaved: () -> Unit = {}) {
        val state = _cardEditorState.value
        val validation = state.draft.validation()
        if (!validation.isValid) {
            _cardEditorState.value = state.copy(error = validation.message)
            return
        }

        viewModelScope.launch {
            val old = state.templateId?.let { repository.getCardTemplate(it) }
            repository.saveCardTemplate(
                state.draft.toTemplateEntity(
                    templateId = state.templateId ?: 0L,
                    createdAt = old?.createdAt ?: System.currentTimeMillis()
                )
            )
            _cardEditorState.update { it.copy(error = null) }
            onSaved()
        }
    }

    private fun saveDraftAsReusableTemplate(draft: CardDraft) {
        viewModelScope.launch {
            repository.saveCardTemplate(draft.toTemplateEntity())
        }
    }

    fun deleteCardTemplate(templateId: Long) {
        viewModelScope.launch { repository.deleteCardTemplate(templateId) }
    }

    fun addTemplateToCurrentFlow(template: CardTemplateEntity) {
        val draft = CardDraft(
            cardTemplateId = template.id,
            orderIndex = _editorState.value.cards.size,
            blocks = ContentBlockJson.decode(template.contentBlocksJson),
            style = template.style,
            hasTimer = template.hasTimer,
            timerMode = template.timerMode,
            fixedTimerDurationSeconds = template.fixedTimerDurationSeconds
        )
        _editorState.update { state ->
            state.copy(
                cards = (state.cards + draft).mapIndexed { index, card -> card.copy(orderIndex = index) },
                error = null
            )
        }
    }

    fun removeCard(index: Int) {
        _editorState.update { state ->
            state.copy(
                cards = state.cards
                    .filterIndexed { i, _ -> i != index }
                    .mapIndexed { i, card -> card.copy(orderIndex = i) }
            )
        }
    }

    fun moveCard(index: Int, delta: Int) {
        val cards = _editorState.value.cards.toMutableList()
        val target = index + delta
        if (index !in cards.indices || target !in cards.indices) return
        val card = cards.removeAt(index)
        cards.add(target, card)
        _editorState.update {
            it.copy(cards = cards.mapIndexed { i, item -> item.copy(orderIndex = i) })
        }
    }

    fun saveExperiment(saveAsNew: Boolean = false, onSaved: (Long) -> Unit = {}) {
        val state = _editorState.value
        val validation = LabValidators.validateExperimentNameAndCards(
            name = state.name,
            cardValidations = state.cards.map { it.validation() }
        )
        if (!validation.isValid) {
            _editorState.value = state.copy(error = validation.message)
            return
        }

        viewModelScope.launch {
            val id = repository.saveExperiment(
                templateId = state.templateId,
                name = state.name,
                tags = state.tags,
                cards = state.cards.map { it.toEntity(state.templateId) },
                saveAsNew = saveAsNew
            )
            _editorState.update { it.copy(templateId = id, lastSavedId = id, error = null) }
            onSaved(id)
        }
    }

    fun copyExperiment(templateId: Long) {
        viewModelScope.launch { repository.copyExperiment(templateId) }
    }

    fun deleteExperiment(templateId: Long) {
        viewModelScope.launch { repository.deleteExperiment(templateId) }
    }

    fun getCardsForTemplate(templateId: Long): StateFlow<List<ExperimentCardEntity>> =
        repository.observeCards(templateId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

private fun ExperimentCardEntity.toDraft(): CardDraft =
    CardDraft(
        id = id,
        experimentTemplateId = experimentTemplateId,
        cardTemplateId = cardTemplateId,
        orderIndex = orderIndex,
        blocks = ContentBlockJson.decode(contentBlocksJson),
        style = style,
        hasTimer = hasTimer,
        timerMode = timerMode,
        fixedTimerDurationSeconds = fixedTimerDurationSeconds,
        createdAt = createdAt
    )

private fun CardTemplateEntity.toDraft(): CardDraft =
    CardDraft(
        cardTemplateId = id,
        blocks = ContentBlockJson.decode(contentBlocksJson),
        style = style,
        hasTimer = hasTimer,
        timerMode = timerMode,
        fixedTimerDurationSeconds = fixedTimerDurationSeconds,
        createdAt = createdAt
    )

private fun CardDraft.toEntity(templateId: Long): ExperimentCardEntity =
    ExperimentCardEntity(
        id = id,
        experimentTemplateId = templateId,
        cardTemplateId = cardTemplateId,
        orderIndex = orderIndex,
        contentBlocksJson = ContentBlockJson.encode(blocks),
        style = style,
        hasTimer = hasTimer,
        timerMode = timerMode,
        fixedTimerDurationSeconds = fixedTimerDurationSeconds,
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis()
    )

private fun CardDraft.toTemplateEntity(
    templateId: Long = 0L,
    createdAt: Long = System.currentTimeMillis()
): CardTemplateEntity =
    CardTemplateEntity(
        id = templateId,
        name = templateName(),
        contentBlocksJson = ContentBlockJson.encode(blocks),
        style = style,
        hasTimer = hasTimer,
        timerMode = timerMode,
        fixedTimerDurationSeconds = fixedTimerDurationSeconds,
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis()
    )

private fun newBlockId(): String = UUID.randomUUID().toString()

private fun CardDraft.templateName(): String {
    val firstText = blocks.filterIsInstance<CardContentBlock.TextBlock>()
        .firstOrNull { it.text.isNotBlank() }
        ?.text
        ?.trim()
        .orEmpty()
    return firstText.take(24).ifBlank { "实验卡片" }
}
