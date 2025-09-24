package com.invi.finerc.models

data class SmsMessageModel(
    val id: Long? = null,
    val address: String,
    val body: String,
    val date: Long,
    val place: String,
    val transactionType: SMSType,
    val bankName: String,
    val category: Category,
    val amount: Double = 0.0,
    val percentage: Double = 0.0,
    val note: String = ""
)

fun SmsMessageModel.toEntity() = SMSTransactionEntity(
    id = id ?: 0L, // Room will auto-generate if 0L
    address = address,
    body = body,
    date = date,
    place = place,
    transactionType = transactionType.name,
    bankName = bankName,
    category = category.name,
    amount = amount,
    percentage = percentage,
    note = note
)

fun SMSTransactionEntity.toModel() = SmsMessageModel(
    id = id,
    address = address,
    body = body,
    date = date,
    place = place,
    transactionType = SMSType.valueOf(transactionType),
    bankName = bankName,
    category = Category.valueOf(category),
    amount = amount,
    percentage = percentage,
    note = note
)
