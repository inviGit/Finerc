package com.invi.finerc.domain.models

data class PaymentSummary(
    val totalPaymentDue: Double,
    val minimumPaymentDue: Double,
    val paymentDueDate: String,
    val paymentDueMillis: Long,
    val statementMonth: Long
)
