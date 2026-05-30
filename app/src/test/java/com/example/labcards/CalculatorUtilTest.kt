package com.example.labcards

import com.example.labcards.util.ConcentrationUnit
import com.example.labcards.util.DilutionUtil
import com.example.labcards.util.LabUnit
import com.example.labcards.util.MassUnit
import com.example.labcards.util.MolarityUtil
import com.example.labcards.util.UnitCategory
import com.example.labcards.util.UnitConversionUtil
import com.example.labcards.util.VolumeUnit
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorUtilTest {
    @Test
    fun dilutionRequiresExactlyOneMissingValue() {
        val result = DilutionUtil.calculate(
            c1Text = "1",
            v1Text = "",
            c2Text = "",
            v2Text = "10",
            c1Unit = ConcentrationUnit.M,
            v1Unit = VolumeUnit.ML,
            c2Unit = ConcentrationUnit.MM,
            v2Unit = VolumeUnit.ML
        )

        assertTrue(result.isError)
    }

    @Test
    fun dilutionCalculatesV1AndDiluent() {
        val result = DilutionUtil.calculate(
            c1Text = "100",
            v1Text = "",
            c2Text = "10",
            v2Text = "10",
            c1Unit = ConcentrationUnit.MM,
            v1Unit = VolumeUnit.ML,
            c2Unit = ConcentrationUnit.MM,
            v2Unit = VolumeUnit.ML
        )

        assertFalse(result.isError)
        assertTrue(result.text.contains("V1"))
        assertTrue(result.text.contains("mL"))
        assertTrue(result.text.contains("Diluent volume"))
    }

    @Test
    fun molarityCalculatesMass() {
        val result = MolarityUtil.calculate(
            massText = "",
            mwText = "100",
            volumeText = "1",
            concentrationText = "1",
            massUnit = MassUnit.G,
            volumeUnit = VolumeUnit.L,
            concentrationUnit = ConcentrationUnit.M
        )

        assertFalse(result.isError)
        assertTrue(result.text.contains("m ="))
        assertTrue(result.text.contains("g"))
    }

    @Test
    fun unitConverterRejectsDifferentCategories() {
        val from = LabUnit("mL", UnitCategory.VOLUME, { it * 1e-3 }, { it / 1e-3 })
        val to = LabUnit("mg", UnitCategory.MASS, { it * 1e-3 }, { it / 1e-3 })

        val result = UnitConversionUtil.convert("1", from, to)

        assertTrue(result.isError)
    }
}
