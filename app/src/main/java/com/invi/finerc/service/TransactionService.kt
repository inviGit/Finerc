package com.invi.finerc.service

import android.util.Log
import com.invi.finerc.data.entity.TransactionEntity
import com.invi.finerc.data.entity.TransactionItemEntity
import com.invi.finerc.data.repository.TransactionRepository
import com.invi.finerc.domain.mapper.TransactionMapper
import com.invi.finerc.domain.models.OrderItemRecord
import com.invi.finerc.domain.models.TransactionUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionService @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    private val entityStateCache = mutableMapOf<Long, TransactionEntity>()

    // Flow version for reactive UI (recommended for Compose)
    fun getTransactionsFlow(): Flow<List<TransactionUiModel>> {
        return transactionRepository.getAllTransactionsFlow()
            .map { entities ->
                Log.d("TransactionService", "Mapping ${entities.size} entities to UI models")
                entities.forEach { entityStateCache[it.id] = it }
                entities.map { TransactionMapper.entityToUiModel(it) }
            }
    }

    suspend fun getTransaction(id: Long): TransactionUiModel? {
        Log.d("TransactionService", "Fetching transaction with id: $id")
        val entity = entityStateCache[id] ?: transactionRepository.getTransaction(id)
        entity?.let { entityStateCache[it.id] = it }
        return entity?.let { TransactionMapper.entityToUiModel(it) }
    }

    suspend fun getAllTransactions(): List<TransactionUiModel> {
        val entities = transactionRepository.getAllTransactions()
        return entities.map { TransactionMapper.entityToUiModel(it) }
    }

    suspend fun getTransactionsByPlace(place: String): List<TransactionUiModel> {
        val entities = transactionRepository.getTransactionsByPlace(place)
        return entities.map { TransactionMapper.entityToUiModel(it) }
    }

    suspend fun getTransactionEntity(id: Long): TransactionEntity? {
        return transactionRepository.getTransaction(id)
    }

    // Save multiple transactions (bulk insert)
    suspend fun saveTransactions(transactions: List<TransactionEntity>) {
        Log.d("TransactionService", "Saving ${transactions.size} transactions")
        transactionRepository.saveTransactions(transactions)
        transactions.forEach { entityStateCache[it.id] = it }
    }

    suspend fun addTransaction(transaction: TransactionUiModel) {
        val entity = TransactionMapper.uiModelToEntity(transaction)
        transactionRepository.saveTransaction(entity)
        entityStateCache[entity.id] = entity
    }

    suspend fun updateTransaction(updatedEntity: TransactionEntity) {
        transactionRepository.saveTransaction(updatedEntity)
        entityStateCache[updatedEntity.id] = updatedEntity
    }

    // Delete transaction
    suspend fun deleteTransaction(id: Long) {
        Log.d("TransactionService", "Deleting transaction with id: $id")
        transactionRepository.deleteMessage(id)
        entityStateCache.remove(id)
    }

    fun clearCache() {
        entityStateCache.clear()
        Log.d("TransactionService", "Entity cache cleared")
    }

    suspend fun linkOrderItemsToTransactions(orderItems: List<OrderItemRecord>) {
        // Helper to truncate ISO datetime to yyyy-MM-dd HH:mm
        fun truncateMillisToDateHourMinute(millis: Long): String {
            val instant = Instant.ofEpochMilli(millis)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneOffset.UTC)
            return formatter.format(instant)
        }

        // Group by truncated datetime (yyyy-MM-dd HH:mm)
        val grouped = orderItems.groupBy { truncateMillisToDateHourMinute(it.orderDate) }
        // orderDateIsoString: String in ISO format ("2025-09-26T19:45:40Z")

        grouped.forEach { (dateHourMinute, items) ->
            // Sum totalOwed for each group
            val transactionAmount = items.sumOf { it.totalOwed }

            // Find a representative order ID if needed (could be from the first item)
            val orderId = items.firstOrNull()?.orderId ?: return@forEach

            // Find matching transaction by place, date +- 1 day, amount
            // Convert truncated string back to millis for date match
            val transactionDateMillis = Instant.from(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneOffset.UTC).parse(dateHourMinute)
            ).toEpochMilli()

            val matchedTransaction = transactionRepository.findMatchingTransaction(
                placePattern = "%amazon%",
                date = transactionDateMillis,
                amount = transactionAmount
            )

            if (matchedTransaction != null) {
                val dbItems = items.map {
                    TransactionItemEntity(
                        transactionId = matchedTransaction.id,
                        orderId = it.orderId,
                        orderDate = it.orderDate,
                        unitPrice = it.unitPrice,
                        unitPriceTax = it.unitPriceTax,
                        shippingCharge = it.shippingCharge,
                        totalDiscount = it.totalDiscount,
                        totalOwed = it.totalOwed,
                        shipmentItemSubtotal = it.shipmentItemSubtotal,
                        shipmentItemSubtotalTax = it.shipmentItemSubtotalTax,
                        quantity = it.quantity,
                        paymentInstrument = it.paymentInstrument,
                        orderStatus = it.orderStatus,
                        productName = it.productName,
                        contractId = "",
                        returnDate = 0L,
                        returnAmount = 0.00,
                        returnReason = "",
                        resolution = "",

                    )
                }

                transactionRepository.saveTransactionItems(dbItems)
            }
        }
    }

    suspend fun getTransactionItems(transactionId: Long): List<TransactionItemEntity> {
        return transactionRepository.getTransactionItems(transactionId)
    }
}
