package com.example.labcards.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.labcards.data.model.CardStyle
import com.example.labcards.ui.viewmodel.CardDraft
import com.example.labcards.ui.viewmodel.ExperimentEditorState
import com.example.labcards.util.CardContentParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowEditorScreen(
    state: ExperimentEditorState,
    onNameChange: (String) -> Unit,
    onTagsChange: (String) -> Unit,
    onAddCard: () -> Unit,
    onEditCard: (Int) -> Unit,
    onDeleteCard: (Int) -> Unit,
    onMoveCard: (Int, Int) -> Unit,
    onSave: () -> Unit,
    onSaveAs: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.templateId == 0L) "新建实验流程" else "编辑实验流程") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(onClick = onSaveAs, enabled = state.cards.isNotEmpty()) {
                        Text("另存为")
                    }
                    IconButton(onClick = onSave) {
                        Icon(Icons.Default.Save, contentDescription = "保存")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCard) {
                Icon(Icons.Default.Add, contentDescription = "添加卡片")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = { Text("实验名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.tags,
                onValueChange = onTagsChange,
                label = { Text("Tag，多个可用逗号分隔") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (state.cards.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("还没有卡片", style = MaterialTheme.typography.titleMedium)
                    Text("点击右下角按钮添加流程内卡片。")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(state.cards) { index, card ->
                        FlowEditorCardItem(
                            index = index,
                            count = state.cards.size,
                            card = card,
                            onEdit = { onEditCard(index) },
                            onDelete = { onDeleteCard(index) },
                            onMoveUp = { onMoveCard(index, -1) },
                            onMoveDown = { onMoveCard(index, 1) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowEditorCardItem(
    index: Int,
    count: Int,
    card: CardDraft,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = styleColor(card.style)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("步骤 ${index + 1}", style = MaterialTheme.typography.labelLarge)
            Text(
                text = CardContentParser.parseToAnnotatedString(card.blocks),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = timerLabel(card),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onMoveUp, enabled = index > 0) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "上移")
                }
                IconButton(onClick = onMoveDown, enabled = index < count - 1) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "下移")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "删除")
                }
                OutlinedButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.padding(2.dp))
                    Text("编辑")
                }
            }
        }
    }
}

@Composable
fun styleColor(style: CardStyle): Color = when (style) {
    CardStyle.NORMAL -> MaterialTheme.colorScheme.surface
    CardStyle.DANGER -> Color(0xFFFFEBEE)
    CardStyle.WARNING -> Color(0xFFFFF8E1)
    CardStyle.INFO -> Color(0xFFE3F2FD)
}

private fun timerLabel(card: CardDraft): String = when {
    !card.hasTimer -> "无计时器"
    card.timerMode == null -> "计时器未配置"
    card.timerMode.name == "BOUND_TO_TIME_INPUT" -> "计时器绑定时间输入框"
    else -> "固定计时器 ${card.fixedTimerDurationSeconds ?: 0} 秒"
}
