package com.invi.finerc.domain.models

data class CollectionModel(
    val id: Long,
    val name: String,
    val transactionCount: Int = 0,
    val itemCount: Int = 0,
    val totalItemAmount: Int = 0,
    val totalTransactionAmount: Double = 0.0,
    val transactions: List<TransactionUiModel> = emptyList<TransactionUiModel>()
)