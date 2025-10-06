package com.invi.finerc.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "credit_cards",
    indices = [
        Index(value = ["cardNumberMasked"], unique = true)
    ]
)
data class CreditCardEntity(
    @PrimaryKey(autoGenerate = true)
    val cardId: Long = 0,

    val bankName: String,
    val cardNumberMasked: String,       // e.g. **** 1234
    val cardType: String,             // Enum: CREDIT, DEBIT
    val cardHolderName: String,

    val creditLimit: Double? = null,
    val statementDay: Int? = null,      // 1-31

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
