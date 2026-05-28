package com.example.labcards.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.labcards.data.model.CardContentBlock
import com.example.labcards.data.model.CardStyle
import com.example.labcards.data.model.TimeInputUnit
import com.example.labcards.ui.viewmodel.CardEditorMode
import com.example.labcards.ui.viewmodel.CardEditorState
import com.example.labcards.util.CardContentParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEditScreen(
    state: CardEditorState,
    onAddNumber: (String?, Int?) -> Unit,
    onAddTime: (String?, Int?) -> Unit,
    onUpdateBlock: (String, CardContentBlock) -> Unit,
    onRemoveBlock: (String) -> Unit,
    onStyleChange: (CardStyle) -> Unit,
    onFixedTimerEnabledChange: (Boolean) -> Unit,
    onFixedTimerChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val hasTimeInput = state.draft.blocks.any { it is CardContentBlock.TimeInputBlock }
    var selectedBlockId by remember { mutableStateOf<String?>(null) }
    var selectedTextOffset by remember { mutableStateOf<Int?>(null) }
    val selectedBlock = state.draft.blocks.firstOrNull { it.id == selectedBlockId }
    val saveText = when {
        state.mode == CardEditorMode.TEMPLATE && state.templateId == null -> "保存到卡片仓库"
        state.mode == CardEditorMode.TEMPLATE -> "更新卡片模板"
        state.index == null -> "保存到当前流程"
        else -> "更新当前卡片"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            state.mode == CardEditorMode.TEMPLATE && state.templateId == null -> "新建卡片模板"
                            state.mode == CardEditorMode.TEMPLATE -> "编辑卡片模板"
                            state.index == null -> "添加卡片"
                            else -> "编辑卡片"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        bottomBar = {
            if (!state.isLoading) {
                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(saveText)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.isLoading) {
                Text("正在加载卡片模板...", style = MaterialTheme.typography.bodyMedium)
            } else {
                InsertSymbolBar(
                    selectedBlock = selectedBlock,
                    hasTimeInput = hasTimeInput,
                    onAddNumber = {
                        onAddNumber(selectedBlockId, selectedTextOffset.takeIf { selectedBlock is CardContentBlock.TextBlock })
                    },
                    onAddTime = {
                        onAddTime(selectedBlockId, selectedTextOffset.takeIf { selectedBlock is CardContentBlock.TextBlock })
                    }
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    state.error?.let { error ->
                        item {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    item {
                        CardContentComposer(
                            blocks = state.draft.blocks,
                            selectedBlockId = selectedBlockId,
                            onSelectBlock = { selectedBlockId = it },
                            onTextCursorChange = { blockId, offset ->
                                selectedBlockId = blockId
                                selectedTextOffset = offset
                            },
                            onUpdateBlock = onUpdateBlock
                        )
                    }

                    selectedBlock?.let { block ->
                        if (block is CardContentBlock.NumberInputBlock || block is CardContentBlock.TimeInputBlock) {
                            item {
                                SelectedSymbolEditor(
                                    block = block,
                                    onUpdateBlock = onUpdateBlock,
                                    onRemoveBlock = {
                                        selectedBlockId = null
                                        selectedTextOffset = null
                                        onRemoveBlock(block.id)
                                    }
                                )
                            }
                        }
                    }

                    item {
                        Text("卡片样式", style = MaterialTheme.typography.titleSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CardStyle.entries.forEach { style ->
                                FilterChip(
                                    selected = state.draft.style == style,
                                    onClick = { onStyleChange(style) },
                                    label = { Text(style.name) }
                                )
                            }
                        }
                    }

                    item {
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("固定计时器", style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            text = if (hasTimeInput) {
                                                "已有时间输入框，计时器会自动绑定"
                                            } else {
                                                "没有时间输入框时，可以设置固定倒计时"
                                            },
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Switch(
                                        checked = state.draft.hasTimer,
                                        onCheckedChange = onFixedTimerEnabledChange,
                                        enabled = !hasTimeInput
                                    )
                                }
                                if (state.draft.hasTimer && !hasTimeInput) {
                                    OutlinedTextField(
                                        value = state.draft.fixedTimerDurationSeconds?.toString().orEmpty(),
                                        onValueChange = onFixedTimerChange,
                                        label = { Text("固定计时器秒数") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text("预览", style = MaterialTheme.typography.titleSmall)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = styleColor(state.draft.style))
                        ) {
                            Text(
                                text = CardContentParser.parseToAnnotatedString(state.draft.blocks),
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InsertSymbolBar(
    selectedBlock: CardContentBlock?,
    hasTimeInput: Boolean,
    onAddNumber: () -> Unit,
    onAddTime: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = selectedBlock?.let { "插入位置：当前选中内容之后" } ?: "插入位置：正文末尾",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAddNumber) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("数字")
                }
                Button(onClick = onAddTime, enabled = !hasTimeInput) {
                    Icon(Icons.Default.AccessTime, contentDescription = null)
                    Text("时间")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CardContentComposer(
    blocks: List<CardContentBlock>,
    selectedBlockId: String?,
    onSelectBlock: (String) -> Unit,
    onTextCursorChange: (String, Int) -> Unit,
    onUpdateBlock: (String, CardContentBlock) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("卡片正文", style = MaterialTheme.typography.titleSmall)
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                blocks.forEach { block ->
                    when (block) {
                        is CardContentBlock.TextBlock -> {
                            InlineTextEditor(
                                block = block,
                                selected = selectedBlockId == block.id,
                                onSelect = { onSelectBlock(block.id) },
                                onCursorChange = { offset -> onTextCursorChange(block.id, offset) },
                                onUpdateBlock = onUpdateBlock
                            )
                        }

                        is CardContentBlock.NumberInputBlock -> {
                            SymbolButton(
                                text = "[${block.value} ${block.unit.orEmpty()}]".replace(" ]", "]"),
                                selected = selectedBlockId == block.id,
                                onClick = { onSelectBlock(block.id) }
                            )
                        }

                        is CardContentBlock.TimeInputBlock -> {
                            SymbolButton(
                                text = "[${CardContentParser.formatTime(block.valueSeconds)}]",
                                selected = selectedBlockId == block.id,
                                onClick = { onSelectBlock(block.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InlineTextEditor(
    block: CardContentBlock.TextBlock,
    selected: Boolean,
    onSelect: () -> Unit,
    onCursorChange: (Int) -> Unit,
    onUpdateBlock: (String, CardContentBlock) -> Unit
) {
    var fieldValue by remember(block.id) {
        mutableStateOf(TextFieldValue(block.text))
    }
    LaunchedEffect(block.text) {
        if (fieldValue.text != block.text) {
            fieldValue = fieldValue.copy(text = block.text)
        }
    }
    val background = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    } else {
        Color.Transparent
    }
    Box(
        modifier = Modifier
            .widthIn(min = 96.dp, max = 320.dp)
            .heightIn(min = 40.dp)
            .background(background, RoundedCornerShape(6.dp))
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        BasicTextField(
            value = fieldValue,
            onValueChange = {
                fieldValue = it
                onCursorChange(it.selection.end)
                onUpdateBlock(block.id, block.copy(text = it.text))
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        onSelect()
                        onCursorChange(fieldValue.selection.end)
                    }
                },
            decorationBox = { innerTextField ->
                if (fieldValue.text.isEmpty()) {
                    Text(
                        text = "输入文字",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
private fun SymbolButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = if (selected) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
    Button(
        onClick = onClick,
        colors = colors
    ) {
        Text(text)
    }
}

@Composable
private fun SelectedSymbolEditor(
    block: CardContentBlock,
    onUpdateBlock: (String, CardContentBlock) -> Unit,
    onRemoveBlock: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when (block) {
                        is CardContentBlock.NumberInputBlock -> "数字占位符"
                        is CardContentBlock.TimeInputBlock -> "时间占位符"
                        is CardContentBlock.TextBlock -> "文字"
                    },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall
                )
                IconButton(onClick = onRemoveBlock) {
                    Icon(Icons.Default.Delete, contentDescription = "删除")
                }
            }

            when (block) {
                is CardContentBlock.NumberInputBlock -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = block.value,
                            onValueChange = { onUpdateBlock(block.id, block.copy(value = it)) },
                            label = { Text("数值") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = block.unit.orEmpty(),
                            onValueChange = { onUpdateBlock(block.id, block.copy(unit = it)) },
                            label = { Text("单位") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is CardContentBlock.TimeInputBlock -> {
                    OutlinedTextField(
                        value = block.valueSeconds.toString(),
                        onValueChange = {
                            onUpdateBlock(
                                block.id,
                                block.copy(
                                    valueSeconds = it.toLongOrNull() ?: 0L,
                                    unit = TimeInputUnit.SECONDS
                                )
                            )
                        },
                        label = { Text("倒计时秒数") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is CardContentBlock.TextBlock -> Unit
            }
        }
    }
}
