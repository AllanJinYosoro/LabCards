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
import androidx.compose.material3.FilterChip
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
import com.example.labcards.util.LabUnit
import com.example.labcards.util.UnitCategory
import com.example.labcards.util.UnitConversionUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(onBack: () -> Unit) {
    var inputValue by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(UnitCategory.VOLUME) }
    var fromUnit by remember { mutableStateOf(UnitConversionUtil.unitsFor(category).first()) }
    var toUnit by remember { mutableStateOf(UnitConversionUtil.unitsFor(category).last()) }
    var result by remember { mutableStateOf<CalculationResult?>(null) }

    fun resetUnits(newCategory: UnitCategory) {
        category = newCategory
        val units = UnitConversionUtil.unitsFor(newCategory)
        fromUnit = units.first()
        toUnit = units.getOrElse(1) { units.first() }
        result = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unit converter") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                UnitCategory.values().forEach { item ->
                    FilterChip(
                        selected = category == item,
                        onClick = { resetUnits(item) },
                        label = { Text(item.label) }
                    )
                }
            }

            OutlinedTextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                label = { Text("Value") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                UnitMenu(
                    title = "From",
                    units = UnitConversionUtil.unitsFor(category),
                    selected = fromUnit,
                    onSelected = { fromUnit = it },
                    modifier = Modifier.weight(1f)
                )
                UnitMenu(
                    title = "To",
                    units = UnitConversionUtil.unitsFor(category),
                    selected = toUnit,
                    onSelected = { toUnit = it },
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = {
                    result = UnitConversionUtil.convert(inputValue, fromUnit, toUnit)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Convert")
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
private fun UnitMenu(
    title: String,
    units: List<LabUnit>,
    selected: LabUnit,
    onSelected: (LabUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.labelSmall)
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(selected.label)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            units.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.label) },
                    onClick = {
                        onSelected(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}
