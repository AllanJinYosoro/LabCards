package com.example.labcards

import com.example.labcards.data.model.CardContentBlock
import com.example.labcards.data.model.TimerMode
import com.example.labcards.domain.LabValidators
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationTest {
    @Test
    fun validatesGoodCard() {
        val result = LabValidators.validateCard(
            blocks = listOf(
                CardContentBlock.TextBlock("t", "加入"),
                CardContentBlock.NumberInputBlock("n", "5", "mL")
            ),
            hasTimer = false,
            timerMode = null,
            fixedTimerDurationSeconds = null
        )

        assertTrue(result.isValid)
    }

    @Test
    fun rejectsEmptyNumberInput() {
        val result = LabValidators.validateCard(
            blocks = listOf(CardContentBlock.NumberInputBlock("n", "", "mL")),
            hasTimer = false,
            timerMode = null,
            fixedTimerDurationSeconds = null
        )

        assertFalse(result.isValid)
    }

    @Test
    fun rejectsMultipleTimeInputs() {
        val result = LabValidators.validateCard(
            blocks = listOf(
                CardContentBlock.TimeInputBlock("t1", 60),
                CardContentBlock.TimeInputBlock("t2", 120)
            ),
            hasTimer = true,
            timerMode = TimerMode.BOUND_TO_TIME_INPUT,
            fixedTimerDurationSeconds = null
        )

        assertFalse(result.isValid)
    }

    @Test
    fun rejectsInvalidFixedTimer() {
        val result = LabValidators.validateCard(
            blocks = listOf(CardContentBlock.TextBlock("t", "等待")),
            hasTimer = true,
            timerMode = TimerMode.FIXED,
            fixedTimerDurationSeconds = 0
        )

        assertFalse(result.isValid)
    }

    @Test
    fun rejectsExperimentWithoutCards() {
        val result = LabValidators.validateExperimentNameAndCards("PCR", emptyList())
        assertFalse(result.isValid)
    }
}
