package com.example.labcards.util

import java.util.Locale

enum class ConcentrationUnit(val label: String, val toMolarFactor: Double) {
    M("M", 1.0),
    MM("mM", 1e-3),
    UM("uM", 1e-6),
    NM("nM", 1e-9)
}

enum class VolumeUnit(val label: String, val toLiterFactor: Double) {
    L("L", 1.0),
    ML("mL", 1e-3),
    UL("uL", 1e-6),
    NL("nL", 1e-9)
}

data class CalculationResult(
    val text: String,
    val isError: Boolean = false
)

object DilutionUtil {
    fun calculate(
        c1Text: String,
        v1Text: String,
        c2Text: String,
        v2Text: String,
        c1Unit: ConcentrationUnit,
        v1Unit: VolumeUnit,
        c2Unit: ConcentrationUnit,
        v2Unit: VolumeUnit
    ): CalculationResult {
        val values = listOf(c1Text, v1Text, c2Text, v2Text)
        if (values.count { it.isBlank() } != 1) {
            return CalculationResult("Exactly one variable must be blank.", true)
        }

        if (values.filter { it.isNotBlank() }.any { it.toPositiveDoubleOrNull() == null }) {
            return CalculationResult("Inputs must be valid numbers greater than 0.", true)
        }

        val c1 = c1Text.toPositiveDoubleOrNull()?.times(c1Unit.toMolarFactor)
        val v1 = v1Text.toPositiveDoubleOrNull()?.times(v1Unit.toLiterFactor)
        val c2 = c2Text.toPositiveDoubleOrNull()?.times(c2Unit.toMolarFactor)
        val v2 = v2Text.toPositiveDoubleOrNull()?.times(v2Unit.toLiterFactor)

        return when {
            c1 == null -> {
                val knownV1 = v1 ?: return CalculationResult("V1 is required.", true)
                val knownC2 = c2 ?: return CalculationResult("C2 is required.", true)
                val knownV2 = v2 ?: return CalculationResult("V2 is required.", true)
                val resultM = knownC2 * knownV2 / knownV1
                CalculationResult("C1 = ${format(resultM / c1Unit.toMolarFactor)} ${c1Unit.label}")
            }

            v1 == null -> {
                val knownC2 = c2 ?: return CalculationResult("C2 is required.", true)
                val knownV2 = v2 ?: return CalculationResult("V2 is required.", true)
                val resultL = knownC2 * knownV2 / c1
                val diluentL = knownV2 - resultL
                CalculationResult(
                    "V1 = ${format(resultL / v1Unit.toLiterFactor)} ${v1Unit.label}\n" +
                        "Diluent volume = ${format(diluentL / v2Unit.toLiterFactor)} ${v2Unit.label}"
                )
            }

            c2 == null -> {
                val knownV1 = v1 ?: return CalculationResult("V1 is required.", true)
                val knownV2 = v2 ?: return CalculationResult("V2 is required.", true)
                val resultM = c1 * knownV1 / knownV2
                CalculationResult("C2 = ${format(resultM / c2Unit.toMolarFactor)} ${c2Unit.label}")
            }

            else -> {
                val knownV1 = v1 ?: return CalculationResult("V1 is required.", true)
                val resultL = c1 * knownV1 / c2
                CalculationResult("V2 = ${format(resultL / v2Unit.toLiterFactor)} ${v2Unit.label}")
            }
        }
    }
}

fun String.toPositiveDoubleOrNull(): Double? {
    val value = trim().toDoubleOrNull() ?: return null
    return if (value > 0.0) value else null
}

fun format(value: Double): String = String.format(Locale.US, "%.6g", value)
