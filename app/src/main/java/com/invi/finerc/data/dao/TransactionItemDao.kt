package com.invi.finerc.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.invi.finerc.data.entity.TransactionItemEntity

@Dao
interface TransactionItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<TransactionItemEntity>)

    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId")
    suspend fun getItemsForTransactionSync(transactionId: Long): List<TransactionItemEntity>

    @Update
    suspend fun updateItems(items: List<TransactionItemEntity>)

    @Delete
    suspend fun deleteItems(items: List<TransactionItemEntity>)
}
