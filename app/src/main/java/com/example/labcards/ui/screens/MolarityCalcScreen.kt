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
import com.example.labcards.util.MassUnit
import com.example.labcards.util.MolarityUtil
import com.example.labcards.util.VolumeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MolarityCalcScreen(onBack: () -> Unit) {
    var mass by remember { mutableStateOf("") }
    var mw by remember { mutableStateOf("") }
    var volume by remember { mutableStateOf("") }
    var concentration by remember { mutableStateOf("") }
    var massUnit by remember { mutableStateOf(MassUnit.MG) }
    var volumeUnit by remember { mutableStateOf(VolumeUnit.ML) }
    var concentrationUnit by remember { mutableStateOf(ConcentrationUnit.MM) }
    var result by remember { mutableStateOf<CalculationResult?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("摩尔浓度换算") },
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
            NumberWithUnitField("质量 m", mass, { mass = it }, massUnit.label) {
                UnitMenu(MassUnit.values().toList(), { it.label }, massUnit) { massUnit = it }
            }
            OutlinedTextField(
                value = mw,
                onValueChange = { mw = it },
                label = { Text("分子量 MW (g/mol)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            NumberWithUnitField("体积 V", volume, { volume = it }, volumeUnit.label) {
                UnitMenu(VolumeUnit.values().filter { it != VolumeUnit.NL }, { it.label }, volumeUnit) { volumeUnit = it }
            }
            NumberWithUnitField("摩尔浓度 C", concentration, { concentration = it }, concentrationUnit.label) {
                UnitMenu(ConcentrationUnit.values().toList(), { it.label }, concentrationUnit) { concentrationUnit = it }
            }

            Button(
                onClick = {
                    result = MolarityUtil.calculate(
                        mass,
                        mw,
                        volume,
                        concentration,
                        massUnit,
                        volumeUnit,
                        concentrationUnit
                    )
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
