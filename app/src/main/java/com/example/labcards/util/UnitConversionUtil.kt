package com.example.labcards.util

enum class UnitCategory(val label: String) {
    VOLUME("体积"),
    MASS("质量"),
    CONCENTRATION("浓度"),
    TIME("时间"),
    TEMPERATURE("温度"),
    LENGTH("长度")
}

data class LabUnit(
    val label: String,
    val category: UnitCategory,
    val toBase: (Double) -> Double,
    val fromBase: (Double) -> Double
)

object UnitConversionUtil {
    val units = listOf(
        LabUnit("L", UnitCategory.VOLUME, { it }, { it }),
        LabUnit("mL", UnitCategory.VOLUME, { it * 1e-3 }, { it / 1e-3 }),
        LabUnit("μL", UnitCategory.VOLUME, { it * 1e-6 }, { it / 1e-6 }),
        LabUnit("nL", UnitCategory.VOLUME, { it * 1e-9 }, { it / 1e-9 }),
        LabUnit("g", UnitCategory.MASS, { it }, { it }),
        LabUnit("mg", UnitCategory.MASS, { it * 1e-3 }, { it / 1e-3 }),
        LabUnit("μg", UnitCategory.MASS, { it * 1e-6 }, { it / 1e-6 }),
        LabUnit("ng", UnitCategory.MASS, { it * 1e-9 }, { it / 1e-9 }),
        LabUnit("M", UnitCategory.CONCENTRATION, { it }, { it }),
        LabUnit("mM", UnitCategory.CONCENTRATION, { it * 1e-3 }, { it / 1e-3 }),
        LabUnit("μM", UnitCategory.CONCENTRATION, { it * 1e-6 }, { it / 1e-6 }),
        LabUnit("nM", UnitCategory.CONCENTRATION, { it * 1e-9 }, { it / 1e-9 }),
        LabUnit("h", UnitCategory.TIME, { it * 3600.0 }, { it / 3600.0 }),
        LabUnit("min", UnitCategory.TIME, { it * 60.0 }, { it / 60.0 }),
        LabUnit("s", UnitCategory.TIME, { it }, { it }),
        LabUnit("℃", UnitCategory.TEMPERATURE, { it + 273.15 }, { it - 273.15 }),
        LabUnit("℉", UnitCategory.TEMPERATURE, { (it - 32.0) * 5.0 / 9.0 + 273.15 }, { (it - 273.15) * 9.0 / 5.0 + 32.0 }),
        LabUnit("K", UnitCategory.TEMPERATURE, { it }, { it }),
        LabUnit("m", UnitCategory.LENGTH, { it }, { it }),
        LabUnit("cm", UnitCategory.LENGTH, { it * 1e-2 }, { it / 1e-2 }),
        LabUnit("mm", UnitCategory.LENGTH, { it * 1e-3 }, { it / 1e-3 }),
        LabUnit("μm", UnitCategory.LENGTH, { it * 1e-6 }, { it / 1e-6 }),
        LabUnit("nm", UnitCategory.LENGTH, { it * 1e-9 }, { it / 1e-9 })
    )

    fun unitsFor(category: UnitCategory): List<LabUnit> = units.filter { it.category == category }

    fun convert(valueText: String, from: LabUnit, to: LabUnit): CalculationResult {
        val value = valueText.trim().toDoubleOrNull()
            ?: return CalculationResult("请输入合法数字", true)
        if (from.category != to.category) return CalculationResult("只能在同一类型的单位之间换算", true)
        val result = to.fromBase(from.toBase(value))
        return CalculationResult("${format(value)} ${from.label} = ${format(result)} ${to.label}")
    }
}
