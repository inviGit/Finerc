package com.invi.finerc.transaction

import android.util.Log
import com.invi.finerc.models.SmsMessageModel
import com.invi.finerc.models.toEntity
import com.invi.finerc.models.toModel
import com.invi.finerc.models.CollectionEntity
import com.invi.finerc.models.CollectionSmsMappingEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmsRepository(
    private val dao: SmsTransactionDao,
    private val collectionDao: CollectionDao
) {
    suspend fun deleteMessage(transactionId: Long) = withContext(Dispatchers.IO) {
        dao.deleteById(transactionId)
    }
    suspend fun getTransactionsByPlace(place: String): List<SmsMessageModel> = withContext(Dispatchers.IO) {
        dao.getByPlace(place).map { it.toModel() }
    }
    suspend fun saveMessages(list: List<SmsMessageModel>) = withContext(Dispatchers.IO) {
        Log.d("SmsRepository", "Saving ${list.size} messages to database")
        dao.upsertAll(list.map { it.toEntity() })
        Log.d("SmsRepository", "Successfully saved ${list.size} messages")
    }

    suspend fun getMessage(id: Long): SmsMessageModel? = withContext(Dispatchers.IO) {
        Log.d("SmsRepository", "Fetching message with id: $id")
        val message = dao.getById(id)?.toModel()
        Log.d("SmsRepository", "Found message: ${message != null}")
        message
    }

    suspend fun getAllMessages(): List<SmsMessageModel> = withContext(Dispatchers.IO) {
        Log.d("SmsRepository", "Fetching all messages from database")
        val messages = dao.getAll().map { it.toModel() }
        Log.d("SmsRepository", "Retrieved ${messages.size} messages from database")
        messages
    }

    suspend fun getMessageCount(): Int = withContext(Dispatchers.IO) {
        val count = dao.getAll().size
        Log.d("SmsRepository", "Total messages in database: $count")
        count
    }

    suspend fun getMessagesForMonth(year: Int, month: Int): List<SmsMessageModel> =
        withContext(Dispatchers.IO) {
            val calendar = java.util.Calendar.getInstance()
            calendar.set(java.util.Calendar.YEAR, year)
            calendar.set(java.util.Calendar.MONTH, month - 1) // Calendar.MONTH is 0-based
            calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val startMillis = calendar.timeInMillis
            calendar.add(java.util.Calendar.MONTH, 1)
            val endMillis = calendar.timeInMillis
            dao.getByDateRange(startMillis, endMillis).map { it.toModel() }
        }

    suspend fun getMessagesForYear(year: Int): List<SmsMessageModel> = withContext(Dispatchers.IO) {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.YEAR, year)
        calendar.set(java.util.Calendar.MONTH, java.util.Calendar.JANUARY)
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startMillis = calendar.timeInMillis
        calendar.set(java.util.Calendar.YEAR, year + 1)
        val endMillis = calendar.timeInMillis
        dao.getByDateRange(startMillis, endMillis).map { it.toModel() }
    }

    /**
     * Save only new transactions (ignore if ID already exists)
     */
    suspend fun saveNewMessages(messages: List<SmsMessageModel>) {
        val existingIds = getAllMessages().mapNotNull { it.id }.toSet()
        val newMessages = messages.filter { it.id == null || it.id !in existingIds }
        if (newMessages.isNotEmpty()) {
            saveMessages(newMessages)
        }
    }

    // Collection operations
    suspend fun createCollection(name: String): Long = withContext(Dispatchers.IO) {
        collectionDao.insertCollection(CollectionEntity(name = name))
    }

    suspend fun getAllCollections(): List<CollectionEntity> = withContext(Dispatchers.IO) {
        collectionDao.getAllCollections()
    }

    suspend fun getCollectionById(id: Long): CollectionEntity? = withContext(Dispatchers.IO) {
        collectionDao.getCollectionById(id)
    }

    suspend fun updateCollectionName(id: Long, name: String) = withContext(Dispatchers.IO) {
        collectionDao.updateCollectionName(id, name)
    }

    suspend fun deleteCollection(id: Long) = withContext(Dispatchers.IO) {
        collectionDao.deleteCollection(id)
    }

    suspend fun addTransactionToCollection(collectionId: Long, smsId: Long) = withContext(Dispatchers.IO) {
        collectionDao.insertMapping(CollectionSmsMappingEntity(collectionId, smsId))
    }

    suspend fun removeTransactionFromCollection(collectionId: Long, smsId: Long) = withContext(Dispatchers.IO) {
        collectionDao.removeMapping(collectionId, smsId)
    }

    suspend fun getTransactionsForCollection(collectionId: Long): List<SmsMessageModel> = withContext(Dispatchers.IO) {
        collectionDao.getTransactionsForCollection(collectionId).map { it.toModel() }
    }

    suspend fun getCollectionsForTransaction(smsId: Long): List<CollectionEntity> = withContext(Dispatchers.IO) {
        collectionDao.getCollectionsForSms(smsId)
    }

    suspend fun getTransactionCountForCollection(collectionId: Long): Int = withContext(Dispatchers.IO) {
        collectionDao.getTransactionCountForCollection(collectionId)
    }
}