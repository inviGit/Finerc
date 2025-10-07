package com.invi.finerc.domain.models

data class TransactionItemModel(
    val itemId: Long = 0,
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
