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
import androidx.compose.foundation.shape.RoundedCornerShape
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
    onEditTemplate: (Long) -> Unit,
    onDeleteTemplate: (Long) -> Unit,
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
                    onUseTemplate = onUseTemplate,
                    onEditTemplate = onEditTemplate,
                    onDeleteTemplate = onDeleteTemplate
                )
            }

            CurrentFlowSection(
                cards = state.cards,
                onAddCard = onAddCard,
                onEditCard = onEditCard,
                onDeleteCard = onDeleteCard,
                onMoveCard = onMoveCard
            )
        }
    }
}

@Composable
private fun SavedTemplateStrip(
    templates: List<CardTemplateEntity>,
    onUseTemplate: (CardTemplateEntity) -> Unit,
    onEditTemplate: (Long) -> Unit,
    onDeleteTemplate: (Long) -> Unit
) {
    var selectedTemplate by remember { mutableStateOf<CardTemplateEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
            .padding(top = 16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(116.dp),
            colors = CardDefaults.cardColors(containerColor = MonetTemplateSection),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
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
                        .height(62.dp)
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
        }
    }

    selectedTemplate?.let { template ->
        TemplateDetailDialog(
            template = template,
            onDismiss = { selectedTemplate = null },
            onEdit = {
                onEditTemplate(template.id)
                selectedTemplate = null
            },
            onDelete = {
                onDeleteTemplate(template.id)
                selectedTemplate = null
            },
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
            .height(56.dp)
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
private fun CurrentFlowSection(
    cards: List<CardDraft>,
    onAddCard: () -> Unit,
    onEditCard: (Int) -> Unit,
    onDeleteCard: (Int) -> Unit,
    onMoveCard: (Int, Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        colors = CardDefaults.cardColors(containerColor = MonetFlowSection),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("已加入当前流程的卡片", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "按顺序执行；点击编辑可继续修改。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (cards.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("当前流程还没有卡片", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "点击右下角 + 添加第一张卡片，或从上方模板加入流程。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onAddCard) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("添加第一张卡片")
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 4.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(cards) { index, card ->
                        FlowEditorCardItem(
                            index = index,
                            count = cards.size,
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
private fun TemplateDetailDialog(
    template: CardTemplateEntity,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "编辑模板")
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "删除模板")
                        }
                    }
                }
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

private val MonetTemplateSection = Color(0xFFEAF4F1)
private val MonetFlowSection = Color(0xFFF3EEF8)
