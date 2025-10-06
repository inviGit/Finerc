package com.invi.finerc.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CreditCardWithCycles(
    @Embedded
    val creditCard: CreditCardEntity,

    @Relation(
        parentColumn = "cardId",
        entityColumn = "cardId"
    )
    val billCycles: List<BillCycleEntity>
)