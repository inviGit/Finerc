package com.invi.finerc.domain.models

import com.invi.finerc.data.entity.TransactionItemEntity

data class TransactionUiModel(
    val id: Long,
    val source: TransactionSource,          // SMS / CC_STATEMENT / BANK_STATEMENT
    val txnType: TransactionType,           // CREDIT / DEBIT
    val amount: Double,                     // Txn Amount
    val txnDate: Long,                      // epoch millis
    val bankName: String,                  // Extracted bank name
    val category: Category,                // Food, Travel, Shopping, etc.
    val place: String,                     // Store name
    val description: String,               // narration, SMS body, remarks
    val note: String,                       // Extra Info
    val currencyCode: CurrencyType,
    val status: TransactionStatus,
    val transactionItem: List<TransactionItemEntity>? = emptyList()
)


