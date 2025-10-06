package com.invi.finerc.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.invi.finerc.domain.models.BillCycleStatus

@Entity(
    tableName = "bill_cycles",
    foreignKeys = [ForeignKey(
        entity = CreditCardEntity::class,
        parentColumns = ["cardId"],
        childColumns = ["cardId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["cardId"]), Index(
        value = ["cardId", "startDate"],
        unique = true
    ), Index(value = ["status"]), Index(value = ["dueDate"])]
)
data class BillCycleEntity(
    @PrimaryKey(autoGenerate = true) val cycleId: Long = 0,

    val cardId: Long,                   // FK to CreditCardEntity

    val startDate: Long,                // epoch millis
    val endDate: Long,                  // epoch millis
    val dueDate: Long,                  // epoch millis

    val statementAmount: Double? = null,
    val statementMonth: Long? = null,
    val minDueAmount: Double? = null,
    val paidAmount: Double? = null,
    val status: BillCycleStatus = BillCycleStatus.OPEN,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
