package com.invi.finerc.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.invi.finerc.data.entity.BillCycleEntity
import com.invi.finerc.data.entity.BillCycleWithTransactions
import com.invi.finerc.domain.models.BillCycleStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BillCycleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycle(cycle: BillCycleEntity): Long

    @Update
    suspend fun updateCycle(cycle: BillCycleEntity)

    @Delete
    suspend fun deleteCycle(cycle: BillCycleEntity)

    @Query("SELECT * FROM bill_cycles WHERE cycleId = :cycleId")
    suspend fun getCycleById(cycleId: Long): BillCycleEntity?

    @Query("SELECT * FROM bill_cycles WHERE cardId = :cardId ORDER BY startDate DESC")
    fun getCyclesByCardFlow(cardId: Long): Flow<List<BillCycleEntity>>

    @Query("SELECT * FROM bill_cycles WHERE status = :status ORDER BY dueDate ASC")
    fun getCyclesByStatusFlow(status: BillCycleStatus): Flow<List<BillCycleEntity>>

    @Transaction
    @Query("SELECT * FROM bill_cycles WHERE cycleId = :cycleId")
    suspend fun getCycleWithTransactions(cycleId: Long): BillCycleWithTransactions?

    @Transaction
    @Query(
        """
        SELECT * FROM bill_cycles 
        WHERE cardId = :cardId 
        ORDER BY startDate DESC
    """
    )
    fun getCyclesWithTransactionsByCardFlow(cardId: Long): Flow<List<BillCycleWithTransactions>>
}