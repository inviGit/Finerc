package com.invi.finerc.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.invi.finerc.data.entity.CreditCardEntity
import com.invi.finerc.data.entity.CreditCardWithCycles
import com.invi.finerc.data.entity.CreditCardWithCyclesAndTransactions
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CreditCardEntity): Long

    @Update
    suspend fun updateCard(card: CreditCardEntity)

    @Delete
    suspend fun deleteCard(card: CreditCardEntity)

    @Query("SELECT * FROM credit_cards WHERE cardId = :cardId")
    suspend fun getCardById(cardId: Long): CreditCardEntity?

    @Query("SELECT * FROM credit_cards ORDER BY bankName ASC")
    fun getAllCardsFlow(): Flow<List<CreditCardEntity>>

    @Transaction
    @Query("SELECT * FROM credit_cards WHERE cardId = :cardId")
    suspend fun getCardWithCycles(cardId: Long): CreditCardWithCycles?

    @Transaction
    @Query("SELECT * FROM credit_cards ORDER BY bankName ASC")
    fun getAllCardsWithCyclesFlow(): Flow<List<CreditCardWithCycles>>

    @Transaction
    @Query("SELECT * FROM credit_cards WHERE cardId = :cardId")
    suspend fun getCardWithCyclesAndTransactions(cardId: Long): CreditCardWithCyclesAndTransactions?
}