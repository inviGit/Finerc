package com.invi.finerc.transaction

import android.content.Context
import android.util.Log
import com.invi.finerc.models.Category
import com.invi.finerc.models.SMSType
import com.invi.finerc.models.SmsMessageModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseHelper(private val context: Context) {
    private val database = SmsTransactionDatabase.getInstance(context)
    private val repository = SmsRepository(database.smsTransactionDao(), database.collectionDao())

    suspend fun initializeDatabase() = withContext(Dispatchers.IO) {
        try {
            Log.d("DatabaseHelper", "Initializing database...")

            // Check if database is empty
            val count = repository.getMessageCount()
            Log.d("DatabaseHelper", "Current message count: $count")

            if (count == 0) {
                Log.d("DatabaseHelper", "Database is empty, adding sample data...")
//                addSampleData()
            } else {
                Log.d("DatabaseHelper", "Database already contains $count messages")
            }

            true
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error initializing database", e)
            false
        }
    }

    suspend fun addSampleData() = withContext(Dispatchers.IO) {
        try {

//            repository.saveMessages(sampleData)
//            Log.d("DatabaseHelper", "Successfully added ${sampleData.size} sample messages")

            // Verify the data was saved
            val newCount = repository.getMessageCount()
            Log.d("DatabaseHelper", "New message count: $newCount")

            true
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error adding sample data", e)
            false
        }
    }

    suspend fun getDatabaseInfo(): DatabaseInfo = withContext(Dispatchers.IO) {
        try {
            val totalMessages = repository.getMessageCount()
            val allMessages = repository.getAllMessages()

            val bankCounts = allMessages.groupBy { it.bankName }.mapValues { it.value.size }
            val categoryCounts = allMessages.groupBy { it.category }.mapValues { it.value.size }
            val totalAmount = allMessages.sumOf { it.amount }

            DatabaseInfo(
                totalMessages = totalMessages,
                totalAmount = totalAmount,
                bankCounts = bankCounts,
                categoryCounts = categoryCounts,
                sampleMessages = allMessages.take(3)
            )
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error getting database info", e)
            DatabaseInfo()
        }
    }

    suspend fun clearDatabase() = withContext(Dispatchers.IO) {
        try {
            database.smsTransactionDao().deleteAll()
            Log.d("DatabaseHelper", "Database cleared successfully")
            true
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error clearing database", e)
            false
        }
    }

    data class DatabaseInfo(
        val totalMessages: Int = 0,
        val totalAmount: Double = 0.0,
        val bankCounts: Map<String, Int> = emptyMap(),
        val categoryCounts: Map<Category, Int> = emptyMap(),
        val sampleMessages: List<SmsMessageModel> = emptyList()
    )
} 