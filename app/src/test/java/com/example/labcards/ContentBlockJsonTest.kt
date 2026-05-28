package com.example.labcards

import com.example.labcards.data.ContentBlockJson
import com.example.labcards.data.model.CardContentBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentBlockJsonTest {
    @Test
    fun roundTripContentBlocks() {
        val blocks = listOf(
            CardContentBlock.TextBlock("t1", "配置药水 A，添加"),
            CardContentBlock.NumberInputBlock("n1", "5", "mL"),
            CardContentBlock.TextBlock("t2", "药水 B，摇匀"),
            CardContentBlock.TimeInputBlock("time1", 180)
        )

        val json = ContentBlockJson.encode(blocks)
        val decoded = ContentBlockJson.decode(json)

        assertEquals(blocks, decoded)
        assertTrue(json.contains("NUMBER_INPUT"))
        assertTrue(json.contains("TIME_INPUT"))
    }
}
