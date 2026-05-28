package com.example.labcards.domain

import com.example.labcards.data.model.CardContentBlock
import com.example.labcards.data.model.TimerMode

data class ValidationResult(
    val isValid: Boolean,
    val message: String? = null
) {
    companion object {
        val Ok = ValidationResult(true)
        fun error(message: String) = ValidationResult(false, message)
    }
}

object LabValidators {
    fun validateCard(
        blocks: List<CardContentBlock>,
        hasTimer: Boolean,
        timerMode: TimerMode?,
        fixedTimerDurationSeconds: Long?
    ): ValidationResult {
        if (blocks.isEmpty()) return ValidationResult.error("卡片内容不能为空")

        val hasVisibleContent = blocks.any {
            when (it) {
                is CardContentBlock.TextBlock -> it.text.isNotBlank()
                is CardContentBlock.NumberInputBlock -> true
                is CardContentBlock.TimeInputBlock -> true
            }
        }
        if (!hasVisibleContent) return ValidationResult.error("卡片内容不能为空")

        blocks.filterIsInstance<CardContentBlock.NumberInputBlock>().forEach {
            if (it.value.isBlank()) return ValidationResult.error("每个数字输入框都必须填写内容")
            if (it.value.toDoubleOrNull() == null) return ValidationResult.error("数字输入框必须是合法数字")
        }

        val timeInputs = blocks.filterIsInstance<CardContentBlock.TimeInputBlock>()
        if (timeInputs.size > 1) return ValidationResult.error("一张卡片最多只能有一个时间输入框")
        timeInputs.forEach {
            if (it.valueSeconds <= 0) return ValidationResult.error("时间输入框必须大于 0 秒")
        }

        if (timeInputs.isNotEmpty()) {
            if (!hasTimer || timerMode != TimerMode.BOUND_TO_TIME_INPUT) {
                return ValidationResult.error("存在时间输入框时，计时器必须绑定到该输入框")
            }
        }

        if (hasTimer) {
            if (timerMode == null) return ValidationResult.error("启用计时器时必须选择计时器模式")
            if (timerMode == TimerMode.FIXED && (fixedTimerDurationSeconds == null || fixedTimerDurationSeconds <= 0)) {
                return ValidationResult.error("固定计时器时间必须大于 0 秒")
            }
        }

        if (!hasTimer && timerMode != null) {
            return ValidationResult.error("未启用计时器时不能设置计时器模式")
        }
        return ValidationResult.Ok
    }

    fun validateExperimentNameAndCards(
        name: String,
        cardValidations: List<ValidationResult>
    ): ValidationResult {
        if (name.isBlank()) return ValidationResult.error("实验名称不能为空")
        if (cardValidations.isEmpty()) return ValidationResult.error("实验流程至少需要一张卡片")
        return cardValidations.firstOrNull { !it.isValid } ?: ValidationResult.Ok
    }
}
