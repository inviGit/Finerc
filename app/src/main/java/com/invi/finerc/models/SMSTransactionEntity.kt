package com.invi.finerc.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_transactions")
data class SMSTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L, // Will be auto-incremented
    @ColumnInfo(name = "address") val address: String,
    @ColumnInfo(name = "body") val body: String,
    @ColumnInfo(name = "date") val date: Long,
    @ColumnInfo(name = "place") val place: String,
    @ColumnInfo(name = "transaction_type") val transactionType: String,
    @ColumnInfo(name = "bank_name") val bankName: String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "amount") val amount: Double = 0.0,
    @ColumnInfo(name = "percentage") val percentage: Double = 0.0,
    @ColumnInfo(name = "note") val note: String = ""
)