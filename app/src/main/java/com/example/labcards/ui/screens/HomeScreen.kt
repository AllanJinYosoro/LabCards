package com.example.labcards.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onNavigateToFlows: () -> Unit,
    onNavigateToCalculator: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "LabCards", style = MaterialTheme.typography.headlineLarge)
        Text(text = "本地实验流程与计算工具", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(40.dp))

        HomeEntry(
            title = "实验流程",
            description = "创建、编辑并执行实验步骤卡片",
            icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
            onClick = onNavigateToFlows
        )
        Spacer(modifier = Modifier.height(16.dp))
        HomeEntry(
            title = "计算器",
            description = "稀释、摩尔浓度与常用单位换算",
            icon = { Icon(Icons.Default.Calculate, contentDescription = null) },
            onClick = onNavigateToCalculator
        )
    }
}

@Composable
private fun HomeEntry(
    title: String,
    description: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon()
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = title, style = MaterialTheme.typography.titleLarge)
                    Text(text = description, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
