package com.invi.finerc.domain.models

data class CollectionUiModel(
    val id: Long,
    val name: String,
    val transactionCount: Int = 0,
    val totalSpent: Double = 0.0,
    val transactions: List<TransactionUiModel> = emptyList<TransactionUiModel>()
)