package com.example.labcards.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.labcards.data.model.CardContentBlock
import com.example.labcards.data.model.CardStyle
import com.example.labcards.data.model.TimeInputUnit
import com.example.labcards.ui.viewmodel.CardEditorState
import com.example.labcards.util.CardContentParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEditScreen(
    state: CardEditorState,
    onAddText: () -> Unit,
    onAddNumber: () -> Unit,
    onAddTime: () -> Unit,
    onUpdateBlock: (String, CardContentBlock) -> Unit,
    onRemoveBlock: (String) -> Unit,
    onStyleChange: (CardStyle) -> Unit,
    onFixedTimerEnabledChange: (Boolean) -> Unit,
    onFixedTimerChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val hasTimeInput = state.draft.blocks.any { it is CardContentBlock.TimeInputBlock }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.index == null) "添加卡片" else "编辑卡片") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onAddText) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("文本")
                    }
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

            items(state.draft.blocks, key = { it.id }) { block ->
                ContentBlockEditor(
                    block = block,
                    onUpdateBlock = onUpdateBlock,
                    onRemoveBlock = onRemoveBlock
                )
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

            item {
                Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                    Text(if (state.index == null) "保存到当前流程" else "更新当前卡片")
                }
            }
        }
    }
}

@Composable
private fun ContentBlockEditor(
    block: CardContentBlock,
    onUpdateBlock: (String, CardContentBlock) -> Unit,
    onRemoveBlock: (String) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when (block) {
                        is CardContentBlock.TextBlock -> "文本块"
                        is CardContentBlock.NumberInputBlock -> "数字输入框"
                        is CardContentBlock.TimeInputBlock -> "时间输入框"
                    },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall
                )
                IconButton(onClick = { onRemoveBlock(block.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = "删除")
                }
            }

            when (block) {
                is CardContentBlock.TextBlock -> {
                    OutlinedTextField(
                        value = block.text,
                        onValueChange = { onUpdateBlock(block.id, block.copy(text = it)) },
                        label = { Text("文本") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

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
            }
        }
    }
}
