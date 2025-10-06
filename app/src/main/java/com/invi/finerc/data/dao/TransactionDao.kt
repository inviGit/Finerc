package com.invi.finerc.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.invi.finerc.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertAll(transactions: List<TransactionEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun upsertAll(list: List<TransactionEntity>)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun upsert(entity: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    fun deleteById(id: Long): Int

    @Query("SELECT * FROM transactions ORDER BY txnDate DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY txnDate DESC")
    suspend fun getAllTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getById(id: Long): TransactionEntity?

    @Query("SELECT COUNT(*) FROM transactions")
    fun getTransactionCount(): Int

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY txnDate DESC")
    suspend fun getByCategory(category: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE bankName = :bankName ORDER BY txnDate DESC")
    suspend fun getByBank(bankName: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE place = :place ORDER BY txnDate DESC")
    suspend fun getByPlace(place: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE txnDate >= :startMillis AND txnDate < :endMillis ORDER BY txnDate DESC")
    fun getByDateRange(startMillis: Long, endMillis: Long): List<TransactionEntity>

    @Query(
        """
        SELECT * FROM transactions 
        WHERE place LIKE :placePattern
          AND txnDate BETWEEN :startDate AND :endDate
          AND amount = :amount
        LIMIT 1
    """
    )
    suspend fun findMatchingTransaction(
        placePattern: String,
        startDate: Long,
        endDate: Long,
        amount: Double
    ): TransactionEntity?

    // Helper method for +/- 1 day
    suspend fun findMatchingTransaction(
        placePattern: String,
        date: Long,
        amount: Double
    ): TransactionEntity? {
        val oneDayMillis = 24 * 60 * 60 * 1000L
        return findMatchingTransaction(
            placePattern,
            date - oneDayMillis,
            date + oneDayMillis,
            amount
        )
    }
}