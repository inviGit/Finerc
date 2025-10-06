package com.invi.finerc.data.dao

import android.content.Context
import android.util.Log
import com.invi.finerc.data.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseHelper(private val context: Context) {
    private val database = TransactionDatabase.Companion.getInstance(context)
    private val repository =
        TransactionRepository(database.transactionDao(), database.collectionDao(), database.transactionItemDao())
    suspend fun initializeDatabase() = withContext(Dispatchers.IO) {
        try {
            Log.d("DatabaseHelper", "Initializing database...")

            // Check if database is empty
            val count = repository.getTransactionsCount()
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
}