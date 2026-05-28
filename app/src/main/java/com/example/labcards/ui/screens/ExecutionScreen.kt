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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.labcards.data.ContentBlockJson
import com.example.labcards.data.model.CardContentBlock
import com.example.labcards.data.model.ExperimentCardEntity
import com.example.labcards.data.model.TimerMode
import com.example.labcards.data.model.TimerState
import com.example.labcards.util.CardContentParser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExecutionScreen(
    cards: List<ExperimentCardEntity>,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var currentIndex by remember(cards) { mutableIntStateOf(0) }
    var completedCount by remember(cards) { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("实验执行 ${completedCount}/${cards.size}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        if (cards.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("这个流程还没有卡片")
            }
        } else if (completedCount >= cards.size) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("实验完成", style = MaterialTheme.typography.headlineMedium)
                Button(onClick = onComplete, modifier = Modifier.padding(top = 16.dp)) {
                    Text("返回流程列表")
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp, 96.dp, 16.dp, 160.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(cards, key = { _, card -> card.id }) { index, card ->
                    ExecutionCardItem(
                        card = card,
                        isCurrent = index == currentIndex,
                        isCompleted = index < completedCount,
                        onFinish = {
                            if (index != currentIndex) return@ExecutionCardItem
                            completedCount += 1
                            if (completedCount >= cards.size) {
                                currentIndex = cards.lastIndex
                            } else {
                                currentIndex = completedCount
                                scope.launch { listState.animateScrollToItem(currentIndex) }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExecutionCardItem(
    card: ExperimentCardEntity,
    isCurrent: Boolean,
    isCompleted: Boolean,
    onFinish: () -> Unit
) {
    val blocks = remember(card.contentBlocksJson) { ContentBlockJson.decode(card.contentBlocksJson) }
    val background = if (isCompleted) Color(0xFFE0E0E0) else styleColor(card.style)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isCurrent) 8.dp else 0.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 8.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isCompleted) "已完成" else if (isCurrent) "当前步骤" else "待执行",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = CardContentParser.parseToAnnotatedString(blocks),
                style = if (isCurrent) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge
            )

            if (isCurrent) {
                val duration = timerDurationFor(card, blocks)
                if (duration != null && duration > 0) {
                    TimerControls(initialDuration = duration)
                }
                Button(onClick = onFinish, modifier = Modifier.align(Alignment.End)) {
                    Text("完成此步骤")
                }
            }
        }
    }
}

@Composable
private fun TimerControls(initialDuration: Long) {
    var timeLeft by remember(initialDuration) { mutableLongStateOf(initialDuration) }
    var timerState by remember(initialDuration) { mutableStateOf(TimerState.IDLE) }
    var customExtend by remember { mutableStateOf("") }

    LaunchedEffect(timerState, timeLeft) {
        if (timerState == TimerState.RUNNING && timeLeft > 0) {
            delay(1_000)
            timeLeft -= 1
            if (timeLeft == 0L) timerState = TimerState.FINISHED
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = CardContentParser.formatTime(timeLeft),
            style = MaterialTheme.typography.displaySmall,
            color = if (timerState == TimerState.FINISHED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
        if (timerState == TimerState.FINISHED) {
            Text("计时结束", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    timerState = if (timerState == TimerState.RUNNING) TimerState.PAUSED else TimerState.RUNNING
                },
                enabled = timeLeft > 0
            ) {
                Text(if (timerState == TimerState.RUNNING) "暂停" else "开始")
            }
            OutlinedButton(
                onClick = {
                    timeLeft = initialDuration
                    timerState = TimerState.IDLE
                }
            ) {
                Text("重置")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { timeLeft += 30; if (timerState == TimerState.FINISHED) timerState = TimerState.PAUSED }) { Text("+30 秒") }
            TextButton(onClick = { timeLeft += 60; if (timerState == TimerState.FINISHED) timerState = TimerState.PAUSED }) { Text("+1 分钟") }
            TextButton(onClick = { timeLeft += 300; if (timerState == TimerState.FINISHED) timerState = TimerState.PAUSED }) { Text("+5 分钟") }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = customExtend,
                onValueChange = { customExtend = it },
                label = { Text("自定义秒数") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.padding(4.dp))
            OutlinedButton(
                onClick = {
                    val add = customExtend.toLongOrNull()
                    if (add != null && add > 0) {
                        timeLeft += add
                        if (timerState == TimerState.FINISHED) timerState = TimerState.PAUSED
                        customExtend = ""
                    }
                }
            ) {
                Text("延长")
            }
        }
    }
}

private fun timerDurationFor(
    card: ExperimentCardEntity,
    blocks: List<CardContentBlock>
): Long? = when {
    !card.hasTimer -> null
    card.timerMode == TimerMode.BOUND_TO_TIME_INPUT ->
        blocks.filterIsInstance<CardContentBlock.TimeInputBlock>().firstOrNull()?.valueSeconds
    card.timerMode == TimerMode.FIXED -> card.fixedTimerDurationSeconds
    else -> null
}
