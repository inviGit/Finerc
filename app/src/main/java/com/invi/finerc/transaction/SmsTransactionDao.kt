package com.invi.finerc.transaction

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.invi.finerc.models.SMSTransactionEntity

@Dao
interface SmsTransactionDao {
    @Query("DELETE FROM sms_transactions WHERE id = :id")
    fun deleteById(id: Long): Int
    
    @Query("SELECT * FROM sms_transactions WHERE place = :place")
    fun getByPlace(place: String): List<SMSTransactionEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(list: List<SMSTransactionEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(entity: SMSTransactionEntity)

    @Query("SELECT * FROM sms_transactions WHERE id = :id")
    fun getById(id: Long): SMSTransactionEntity?

    @Query("SELECT * FROM sms_transactions ORDER BY date DESC")
    fun getAll(): List<SMSTransactionEntity>

    // Debug methods
    @Query("SELECT COUNT(*) FROM sms_transactions")
    fun getCount(): Int

    @Query("SELECT * FROM sms_transactions LIMIT 5")
    fun getFirstFive(): List<SMSTransactionEntity>

    @Query("SELECT * FROM sms_transactions WHERE date >= :startMillis AND date < :endMillis ORDER BY date DESC")
    fun getByDateRange(startMillis: Long, endMillis: Long): List<SMSTransactionEntity>

    @Query("DELETE FROM sms_transactions")
    fun deleteAll(): Int
}