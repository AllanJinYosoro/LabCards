package com.example.labcards.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.labcards.data.ContentBlockJson
import com.example.labcards.data.model.CardStyle
import com.example.labcards.data.model.CardTemplateEntity
import com.example.labcards.data.model.TimerMode
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
    savedTemplates: List<CardTemplateEntity>,
    onUseTemplate: (CardTemplateEntity) -> Unit,
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
                        Icon(Icons.Default.Save, contentDescription = "保存流程")
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

            if (savedTemplates.isNotEmpty()) {
                SavedTemplateStrip(
                    templates = savedTemplates,
                    onUseTemplate = onUseTemplate
                )
            }

            if (state.cards.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("当前流程还没有卡片", style = MaterialTheme.typography.titleMedium)
                    Text("保存卡片后，它会显示在这里，并作为当前流程的步骤使用。")
                    Button(
                        onClick = onAddCard,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("添加第一张卡片")
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("已加入当前流程的卡片", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "这些卡片会按顺序用于实验执行。点击编辑可继续修改。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(onClick = onAddCard) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("继续添加")
                    }
                }
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 12.dp),
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
private fun SavedTemplateStrip(
    templates: List<CardTemplateEntity>,
    onUseTemplate: (CardTemplateEntity) -> Unit
) {
    var selectedTemplate by remember { mutableStateOf<CardTemplateEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
            .padding(top = 16.dp)
    ) {
        Text(
            text = "已保存的卡片模板",
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "左右滑动查看模板，点击名称查看详情。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 8.dp)
        ) {
            items(templates, key = { it.id }) { template ->
                TemplateNameCard(
                    template = template,
                    onClick = { selectedTemplate = template }
                )
            }
        }
    }

    selectedTemplate?.let { template ->
        TemplateDetailDialog(
            template = template,
            onDismiss = { selectedTemplate = null },
            onUse = {
                onUseTemplate(template)
                selectedTemplate = null
            }
        )
    }
}

@Composable
private fun TemplateNameCard(
    template: CardTemplateEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(148.dp)
            .height(68.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = styleColor(template.style)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = template.name,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TemplateDetailDialog(
    template: CardTemplateEntity,
    onDismiss: () -> Unit,
    onUse: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            colors = CardDefaults.cardColors(containerColor = styleColor(template.style)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(template.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = CardContentParser.parseToAnnotatedString(
                        ContentBlockJson.decode(template.contentBlocksJson)
                    ),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = timerLabel(template),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(onClick = onUse, modifier = Modifier.fillMaxWidth()) {
                    Text("加入流程")
                }
            }
        }
    }
}

private fun timerLabel(template: CardTemplateEntity): String = when {
    !template.hasTimer -> "无计时器"
    template.timerMode == null -> "计时器未配置"
    template.timerMode == TimerMode.BOUND_TO_TIME_INPUT -> "计时器绑定时间输入框"
    else -> "固定计时器 ${template.fixedTimerDurationSeconds ?: 0} 秒"
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
    card.timerMode == TimerMode.BOUND_TO_TIME_INPUT -> "计时器绑定时间输入框"
    else -> "固定计时器 ${card.fixedTimerDurationSeconds ?: 0} 秒"
}
