package com.invi.finerc.domain.models

data class TransactionsUiState(
    val transactions: List<TransactionUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)