package com.invi.finerc.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.invi.finerc.domain.models.Category
import com.invi.finerc.domain.models.CurrencyType
import com.invi.finerc.domain.models.TransactionSource
import com.invi.finerc.domain.models.TransactionStatus
import com.invi.finerc.domain.models.TransactionType

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = BillCycleEntity::class,
            parentColumns = ["cycleId"],
            childColumns = ["cycleId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["cycleId"]),
        Index(value = ["transactionId"], unique = true)
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,   // internal PK

    val transactionId: String,              // unique external/business ID
    val cycleId: Long? = null,                     // FK to BillCycleEntity
    val accountId: Long? = null,                   // TODO: FK to BankAccountEntity

    val source: TransactionSource? = null,          // SMS / CC_STATEMENT / BANK_STATEMENT
    val txnType: TransactionType? = null,           // CREDIT / DEBIT
    val amount: Double,                     // Txn Amount
    val cashback: Double? = null,                     // Txn Amount
    val txnDate: Long,                      // epoch millis
    val bankName: String? = null,                  // Extracted bank name
    val category: Category? = null,                // Food, Travel, Shopping, etc.
    val place: String? = null,                     // Store name
    val description: String? = null,               // narration, SMS body, remarks
    val note: String? = null,                       // Extra Info
    val currencyCode: CurrencyType? = CurrencyType.INR,
    val status: TransactionStatus? = TransactionStatus.ACTIVE,

    // Meta
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
