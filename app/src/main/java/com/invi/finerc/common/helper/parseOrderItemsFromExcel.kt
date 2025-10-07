package com.invi.finerc.common.helper

import com.invi.finerc.domain.models.TransactionItemModel
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream
import java.time.Instant

suspend fun parseOrderItemsFromExcel(inputStream: InputStream): List<TransactionItemModel> {
    val workbook = WorkbookFactory.create(inputStream)
    val sheet = workbook.getSheetAt(0) // Assuming first sheet

    val orderItems = mutableListOf<TransactionItemModel>()

    val headerRow = sheet.getRow(0)
    val headers = headerRow.map { it.stringCellValue.trim() }

    // Get column indexes for needed columns from header
    val orderIdCol = headers.indexOf("Order ID")
    val orderDateCol = headers.indexOf("Order Date")
    val unitPriceCol = headers.indexOf("Unit Price")
    val unitPriceTaxCol = headers.indexOf("Unit Price Tax")
    val shippingChargeCol = headers.indexOf("Shipping Charge")
    val totalDiscountsCol = headers.indexOf("Total Discounts")
    val totalOwedCol = headers.indexOf("Total Owed")
    val shipmentItemSubtotalCol = headers.indexOf("Shipment Item Subtotal")
    val shipmentItemSubtotalTaxCol = headers.indexOf("Shipment Item Subtotal Tax")
    val quantityCol = headers.indexOf("Quantity")
    val paymentInstrumentTypeCol = headers.indexOf("Payment Instrument Type")
    val orderStatusCol = headers.indexOf("Order Status")
    val productNameCol = headers.indexOf("Product Name")

    fun parseAndValidateNumber(cellValue: Any?): Double {
        if (cellValue == null) return 0.0
        val strVal = cellValue.toString().trim().trim('\'', '\"')
        return try {
            val number = strVal.removePrefix("-").toDouble()
            if (number % 1 == 0.0) number.toInt().toDouble() else number
        } catch (e: NumberFormatException) {
            0.0
        }
    }

    // Parse rows
    for (rowIndex in 1..sheet.lastRowNum) {
        val row = sheet.getRow(rowIndex) ?: continue

        val orderId = row.getCell(orderIdCol)?.stringCellValue ?: continue

        val dateCell = row.getCell(orderDateCol)
        val orderDate: Long = when (dateCell.cellType) {
            CellType.NUMERIC -> dateCell.dateCellValue.time
            CellType.STRING -> {
                Instant.parse(dateCell.stringCellValue).toEpochMilli()
            }

            else -> continue
        }

        val unitPrice = parseAndValidateNumber(row.getCell(unitPriceCol)?.toString())
        val unitPriceTax = parseAndValidateNumber(row.getCell(unitPriceTaxCol)?.toString())
        val shippingCharge = parseAndValidateNumber(row.getCell(shippingChargeCol)?.toString())
        val totalDiscounts = parseAndValidateNumber(row.getCell(totalDiscountsCol)?.toString())
        val totalOwed = parseAndValidateNumber(row.getCell(totalOwedCol)?.toString())
        val shipmentSubtotal =
            parseAndValidateNumber(row.getCell(shipmentItemSubtotalCol)?.toString())
        val shipmentSubtotalTax =
            parseAndValidateNumber(row.getCell(shipmentItemSubtotalTaxCol)?.toString())
        val quantity = parseAndValidateNumber(row.getCell(quantityCol)?.toString()).toInt()
        val paymentInstrumentType = row.getCell(paymentInstrumentTypeCol)?.stringCellValue ?: ""
        val orderStatus = row.getCell(orderStatusCol)?.stringCellValue ?: ""
        val productName = row.getCell(productNameCol)?.stringCellValue ?: ""

        orderItems.add(
            TransactionItemModel(
                orderId = orderId,
                orderDate = orderDate,
                unitPrice = unitPrice,
                unitPriceTax = unitPriceTax,
                shippingCharge = shippingCharge,
                totalDiscount = totalDiscounts,
                totalOwed = totalOwed,
                shipmentItemSubtotal = shipmentSubtotal,
                shipmentItemSubtotalTax = shipmentSubtotalTax,
                quantity = quantity,
                paymentInstrument = paymentInstrumentType,
                orderStatus = orderStatus,
                productName = productName,
                contractId = "",
                returnDate = 0L,
                returnAmount = 0.00,
                returnReason = "",
                resolution = "",
            )
        )
    }
    workbook.close()
    return orderItems
}
