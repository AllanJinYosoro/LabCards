package com.example.labcards.util

enum class MassUnit(val label: String, val toGramFactor: Double) {
    G("g", 1.0),
    MG("mg", 1e-3),
    UG("μg", 1e-6),
    NG("ng", 1e-9)
}

object MolarityUtil {
    fun calculate(
        massText: String,
        mwText: String,
        volumeText: String,
        concentrationText: String,
        massUnit: MassUnit,
        volumeUnit: VolumeUnit,
        concentrationUnit: ConcentrationUnit
    ): CalculationResult {
        val values = listOf(massText, mwText, volumeText, concentrationText)
        if (values.count { it.isBlank() } != 1) return CalculationResult("必须恰好留空一个变量", true)
        if (values.filter { it.isNotBlank() }.any { it.toPositiveDoubleOrNull() == null }) {
            return CalculationResult("输入必须是大于 0 的合法数字", true)
        }

        val massG = massText.toPositiveDoubleOrNull()?.times(massUnit.toGramFactor)
        val mw = mwText.toPositiveDoubleOrNull()
        val volumeL = volumeText.toPositiveDoubleOrNull()?.times(volumeUnit.toLiterFactor)
        val concentrationM = concentrationText.toPositiveDoubleOrNull()?.times(concentrationUnit.toMolarFactor)

        return when {
            massG == null -> {
                val resultG = concentrationM!! * volumeL!! * mw!!
                CalculationResult("m = ${format(resultG / massUnit.toGramFactor)} ${massUnit.label}")
            }
            mw == null -> {
                val result = massG / (concentrationM!! * volumeL!!)
                CalculationResult("MW = ${format(result)} g/mol")
            }
            volumeL == null -> {
                val resultL = massG / (mw!! * concentrationM!!)
                CalculationResult("V = ${format(resultL / volumeUnit.toLiterFactor)} ${volumeUnit.label}")
            }
            else -> {
                val resultM = massG / (mw!! * volumeL)
                CalculationResult("C = ${format(resultM / concentrationUnit.toMolarFactor)} ${concentrationUnit.label}")
            }
        }
    }
}
