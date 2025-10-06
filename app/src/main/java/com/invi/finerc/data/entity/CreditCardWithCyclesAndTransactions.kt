package com.invi.finerc.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CreditCardWithCyclesAndTransactions(
    @Embedded
    val creditCard: CreditCardEntity,

    @Relation(
        entity = BillCycleEntity::class,
        parentColumn = "cardId",
        entityColumn = "cardId"
    )
    val cyclesWithTransactions: List<BillCycleWithTransactions>
)