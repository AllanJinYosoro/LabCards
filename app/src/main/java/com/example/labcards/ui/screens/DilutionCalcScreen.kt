package com.example.labcards.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.labcards.util.CalculationResult
import com.example.labcards.util.ConcentrationUnit
import com.example.labcards.util.DilutionUtil
import com.example.labcards.util.VolumeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DilutionCalcScreen(onBack: () -> Unit) {
    var c1 by remember { mutableStateOf("") }
    var v1 by remember { mutableStateOf("") }
    var c2 by remember { mutableStateOf("") }
    var v2 by remember { mutableStateOf("") }
    var c1Unit by remember { mutableStateOf(ConcentrationUnit.M) }
    var c2Unit by remember { mutableStateOf(ConcentrationUnit.M) }
    var v1Unit by remember { mutableStateOf(VolumeUnit.ML) }
    var v2Unit by remember { mutableStateOf(VolumeUnit.ML) }
    var result by remember { mutableStateOf<CalculationResult?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("稀释与浓度换算") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NumberWithUnitField("C1 初始浓度", c1, { c1 = it }, c1Unit.label) {
                UnitMenu(ConcentrationUnit.values().toList(), { it.label }, c1Unit) { c1Unit = it }
            }
            NumberWithUnitField("V1 初始体积", v1, { v1 = it }, v1Unit.label) {
                UnitMenu(VolumeUnit.values().filter { it != VolumeUnit.NL }, { it.label }, v1Unit) { v1Unit = it }
            }
            NumberWithUnitField("C2 目标浓度", c2, { c2 = it }, c2Unit.label) {
                UnitMenu(ConcentrationUnit.values().toList(), { it.label }, c2Unit) { c2Unit = it }
            }
            NumberWithUnitField("V2 目标体积", v2, { v2 = it }, v2Unit.label) {
                UnitMenu(VolumeUnit.values().filter { it != VolumeUnit.NL }, { it.label }, v2Unit) { v2Unit = it }
            }
            Button(
                onClick = {
                    result = DilutionUtil.calculate(c1, v1, c2, v2, c1Unit, v1Unit, c2Unit, v2Unit)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("计算")
            }
            result?.let {
                Text(
                    text = it.text,
                    color = if (it.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun NumberWithUnitField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    unitLabel: String,
    unitMenu: @Composable () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
        )
        Column {
            Text(unitLabel, style = MaterialTheme.typography.labelSmall)
            unitMenu()
        }
    }
}

@Composable
private fun <T> UnitMenu(
    values: List<T>,
    label: (T) -> String,
    selected: T,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { expanded = true }) { Text(label(selected)) }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        values.forEach { item ->
            DropdownMenuItem(
                text = { Text(label(item)) },
                onClick = {
                    onSelected(item)
                    expanded = false
                }
            )
        }
    }
}
