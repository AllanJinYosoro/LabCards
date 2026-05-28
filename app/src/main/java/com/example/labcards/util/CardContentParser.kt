package com.example.labcards.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.example.labcards.data.model.CardContentBlock

object CardContentParser {
    fun parseToAnnotatedString(blocks: List<CardContentBlock>): AnnotatedString {
        return buildAnnotatedString {
            blocks.forEach { block ->
                when (block) {
                    is CardContentBlock.TextBlock -> append(block.text)
                    is CardContentBlock.NumberInputBlock -> {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(" [${block.value}${block.unit.orEmpty()}] ")
                        }
                    }
                    is CardContentBlock.TimeInputBlock -> {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(" [${formatTime(block.valueSeconds)}] ")
                        }
                    }
                }
            }
        }
    }

    fun formatTime(seconds: Long): String {
        val mins = seconds / 60
        val secs = seconds % 60
        val hours = mins / 60
        val remainMins = mins % 60
        return when {
            hours > 0 -> "${hours}h ${remainMins}min ${secs}s"
            mins > 0 -> "${mins}min ${secs}s"
            else -> "${secs}s"
        }
    }
}
