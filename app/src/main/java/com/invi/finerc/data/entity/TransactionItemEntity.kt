package com.invi.finerc.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transaction_items",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("transactionId")]
)
data class TransactionItemEntity(
    @PrimaryKey(autoGenerate = true) val itemId: Long = 0,
    val transactionId: Long,
    val orderId: String,
    val orderDate: Long,
    val unitPrice: Double,
    val unitPriceTax: Double,
    val shippingCharge: Double,
    val totalDiscount: Double,
    val totalOwed: Double,
    val shipmentItemSubtotal: Double,
    val shipmentItemSubtotalTax: Double,
    val quantity: Int,
    val paymentInstrument: String,
    val orderStatus: String,
    val productName: String,
    val contractId: String,
    val returnDate: Long,
    val returnAmount: Double,
    val returnReason: String,
    val resolution: String
)
