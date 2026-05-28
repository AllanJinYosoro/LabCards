package com.example.labcards.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "flow_nodes")
@Serializable
data class FlowNodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val experimentTemplateId: Long,
    val nodeType: FlowNodeType,
    val cardId: Long? = null,
    val parentNodeId: Long? = null,
    val nextNodeId: Long? = null,
    val branchTrueNodeId: Long? = null,
    val branchFalseNodeId: Long? = null,
    val loopBodyNodeIdsJson: String? = null,
    val conditionDescription: String? = null,
    val loopCount: Int? = null,
    val orderIndex: Int = 0
)

enum class FlowNodeType {
    CARD, IF, FOR, WHILE
}
