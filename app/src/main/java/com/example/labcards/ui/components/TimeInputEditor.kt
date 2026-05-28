package com.example.labcards.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.labcards.data.model.CardContentBlock
import com.example.labcards.data.model.TimeInputUnit

@Composable
fun TimeInputEditor(
    block: CardContentBlock.TimeInputBlock,
    onUpdateBlock: (String, CardContentBlock) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = timeInputDisplayValue(block),
            onValueChange = {
                onUpdateBlock(
                    block.id,
                    block.copy(
                        valueSeconds = parseTimeInputSeconds(it, block.unit),
                        unit = block.unit
                    )
                )
            },
            label = { Text("时间") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = block.unit == TimeInputUnit.MINUTES,
            onClick = {
                onUpdateBlock(block.id, block.copy(unit = TimeInputUnit.MINUTES))
            },
            label = { Text("min") }
        )
        FilterChip(
            selected = block.unit == TimeInputUnit.SECONDS,
            onClick = {
                onUpdateBlock(block.id, block.copy(unit = TimeInputUnit.SECONDS))
            },
            label = { Text("s") }
        )
    }
}

private fun timeInputDisplayValue(block: CardContentBlock.TimeInputBlock): String =
    when (block.unit) {
        TimeInputUnit.MINUTES -> (block.valueSeconds / 60).toString()
        else -> block.valueSeconds.toString()
    }

private fun parseTimeInputSeconds(value: String, unit: TimeInputUnit): Long {
    val number = value.toLongOrNull() ?: 0L
    return when (unit) {
        TimeInputUnit.MINUTES -> number * 60
        else -> number
    }
}
