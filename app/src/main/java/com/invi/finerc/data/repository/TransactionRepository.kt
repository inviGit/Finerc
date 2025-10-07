package com.invi.finerc.data.repository

import android.util.Log
import com.invi.finerc.data.dao.CollectionDao
import com.invi.finerc.data.dao.TransactionDao
import com.invi.finerc.data.dao.TransactionItemDao
import com.invi.finerc.data.entity.CollectionEntity
import com.invi.finerc.data.entity.CollectionItemExclusionEntity
import com.invi.finerc.data.entity.CollectionSmsMappingEntity
import com.invi.finerc.data.entity.CollectionWithStatsEntity
import com.invi.finerc.data.entity.CollectionWithTransactionsEntity
import com.invi.finerc.data.entity.TransactionEntity
import com.invi.finerc.data.entity.TransactionItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val dao: TransactionDao,
    private val collectionDao: CollectionDao,
    private val transactionItemDao: TransactionItemDao
) {

    // ========== Transaction Operations ==========
    // Reactive Flow for UI
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>> {
        Log.d("TransactionRepository", "Setting up transactions flow")
        return dao.getAllTransactionsFlow()
    }

    // One-time queries
    suspend fun getAllTransactions(): List<TransactionEntity> = withContext(Dispatchers.IO) {
        Log.d("TransactionRepository", "Fetching all transactions")
        val transactions = dao.getAllTransactions()
        Log.d("TransactionRepository", "Retrieved ${transactions.size} transactions")
        transactions
    }

    suspend fun getTransaction(id: Long): TransactionEntity? = withContext(Dispatchers.IO) {
        Log.d("TransactionRepository", "Fetching transaction with id: $id")
        dao.getById(id)
    }

    suspend fun getTransactionsByPlace(place: String): List<TransactionEntity> =
        withContext(Dispatchers.IO) {
            dao.getByPlace(place)
        }

    suspend fun getTransactionsCount(): Int = withContext(Dispatchers.IO) {
        val count = dao.getTransactionCount()
        Log.d("TransactionRepository", "Total transactions: $count")
        count
    }

    suspend fun saveTransactions(list: List<TransactionEntity>) = withContext(Dispatchers.IO) {
        Log.d("TransactionRepository", "Saving ${list.size} transactions")
        val ids = dao.insertAll(list)
        val successCount = ids.count { it != -1L }
        Log.d("TransactionRepository", "Successfully saved $successCount/${list.size} transactions")
    }

    suspend fun saveTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        Log.d("TransactionRepository", "Saving single transaction")
        dao.upsert(transaction)
        Log.d("TransactionRepository", "Transaction saved successfully")
    }

    suspend fun deleteMessage(transactionId: Long) = withContext(Dispatchers.IO) {
        Log.d("TransactionRepository", "Deleting transaction id: $transactionId")
        dao.deleteById(transactionId)
    }

    // ========== Transaction Item Operations ==========
    suspend fun saveTransactionItems(items: List<TransactionItemEntity>) =
        withContext(Dispatchers.IO) {
            transactionItemDao.insertItems(items)
        }

    suspend fun getTransactionItems(transactionId: Long): List<TransactionItemEntity> =
        withContext(Dispatchers.IO) {
            transactionItemDao.getItemsForTransactionSync(transactionId)
        }

    // ========== Collection Operations ==========
    fun getCollectionsWithStatsFlow(): Flow<List<CollectionWithStatsEntity>> {
        return collectionDao.getCollectionsWithStatsFlow()
    }

    fun getCollectionWithTransactionsFlow(collectionId: Long): Flow<CollectionWithTransactionsEntity> {
        return collectionDao.getCollectionWithTransactionsFlow(collectionId)
    }

    suspend fun createCollection(name: String): Long = withContext(Dispatchers.IO) {
        collectionDao.insertCollection(CollectionEntity(name = name))
    }

    suspend fun addTransactionsToCollection(collectionSmsMappingEntityList: List<CollectionSmsMappingEntity>) =
        withContext(Dispatchers.IO) {
            collectionDao.insertMapping(collectionSmsMappingEntityList)
        }

    suspend fun removeTransactionFromCollection(collectionId: Long, transactionId: Long) =
        withContext(Dispatchers.IO) {
            collectionDao.removeMapping(collectionId, transactionId)
        }

    suspend fun getTransactionsForCollection(collectionId: Long): List<TransactionEntity> =
        withContext(Dispatchers.IO) {
            collectionDao.getTransactionsForCollection(collectionId)
        }

    suspend fun getCollectionsForTransaction(txnId: Long): List<CollectionEntity> =
        withContext(Dispatchers.IO) {
            collectionDao.getCollectionsForSms(txnId)
        }

    suspend fun updateCollection(id: Long, name: String) =
        withContext(Dispatchers.IO) {
            collectionDao.updateCollectionName(id, name)
        }

    suspend fun findMatchingTransaction(
        placePattern: String, date: Long, amount: Double
    ): TransactionEntity? = withContext(Dispatchers.IO) {
        dao.findMatchingTransaction(placePattern, date, amount)
    }

    fun getExcludedItemsFlow(collectionId: Long): Flow<List<CollectionItemExclusionEntity>> {
        return collectionDao.getExcludedItemsFlow(collectionId)
    }

    suspend fun getExcludedItems(collectionId: Long): List<CollectionItemExclusionEntity> {
        return collectionDao.getExcludedItems(collectionId)
    }

    suspend fun insertExclusion(exclusion: CollectionItemExclusionEntity) =
        withContext(Dispatchers.IO) {
            collectionDao.insertExclusion(exclusion)
        }

    suspend fun deleteExclusion(exclusion: CollectionItemExclusionEntity) =
        withContext(Dispatchers.IO) {
            collectionDao.deleteExclusion(exclusion)
        }

    suspend fun deleteExclusionByIds(collectionId: Long, itemId: Long) =
        withContext(Dispatchers.IO) {
            collectionDao.deleteExclusionByIds(collectionId, itemId)
        }
}
