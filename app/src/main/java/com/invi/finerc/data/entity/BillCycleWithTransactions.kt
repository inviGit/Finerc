package com.invi.finerc.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class BillCycleWithTransactions(
    @Embedded
    val billCycle: BillCycleEntity,

    @Relation(
        parentColumn = "cycleId",
        entityColumn = "cycleId"
    )
    val transactions: List<TransactionEntity>
)