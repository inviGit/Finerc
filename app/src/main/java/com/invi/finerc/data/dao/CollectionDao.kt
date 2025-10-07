package com.invi.finerc.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.invi.finerc.data.entity.CollectionEntity
import com.invi.finerc.data.entity.CollectionItemExclusionEntity
import com.invi.finerc.data.entity.CollectionSmsMappingEntity
import com.invi.finerc.data.entity.CollectionWithStatsEntity
import com.invi.finerc.data.entity.CollectionWithTransactionsEntity
import com.invi.finerc.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {

    @Query(
        """
        SELECT c.*, 
            COUNT(t.id) AS transactionCount,
            COALESCE(SUM(t.amount), 0) AS totalAmount
        FROM collections c
        LEFT JOIN collection_txn_mapping m ON c.id = m.collection_id
        LEFT JOIN transactions t ON t.id = m.txn_id
        GROUP BY c.id, c.name
    """
    )
    fun getCollectionsWithStatsFlow(): Flow<List<CollectionWithStatsEntity>>

    @Transaction
    @Query("SELECT * FROM collections WHERE id = :collectionId")
    fun getCollectionWithTransactionsFlow(collectionId: Long): Flow<CollectionWithTransactionsEntity>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insertCollection(collection: CollectionEntity): Long

    @Query("SELECT * FROM collections ORDER BY name ASC")
    fun getAllCollections(): List<CollectionEntity>

    @Query("SELECT * FROM collections WHERE id = :id")
    fun getCollectionById(id: Long): CollectionEntity?

    @Query("UPDATE collections SET name = :name WHERE id = :id")
    fun updateCollectionName(id: Long, name: String): Int

    @Query("DELETE FROM collections WHERE id = :id")
    fun deleteCollection(id: Long): Int

    // Mapping operations
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insertMapping(collectionSmsMappingEntityList: List<CollectionSmsMappingEntity>)

    @Query("DELETE FROM collection_txn_mapping WHERE collection_id = :collectionId AND txn_id = :smsId")
    fun removeMapping(collectionId: Long, smsId: Long): Int

    @Query("DELETE FROM collection_txn_mapping WHERE collection_id = :collectionId")
    fun removeAllMappingsForCollection(collectionId: Long): Int

    @Query("DELETE FROM collection_txn_mapping WHERE txn_id = :smsId")
    fun removeAllMappingsForSms(smsId: Long): Int

    // Query operations
    @Query("SELECT s.* FROM transactions s INNER JOIN collection_txn_mapping m ON s.id = m.txn_id WHERE m.collection_id = :collectionId ORDER BY s.txnDate DESC")
    fun getTransactionsForCollection(collectionId: Long): List<TransactionEntity>

    @Query("SELECT c.* FROM collections c INNER JOIN collection_txn_mapping m ON c.id = m.collection_id WHERE m.txn_id = :smsId")
    fun getCollectionsForSms(smsId: Long): List<CollectionEntity>

    @Query("SELECT COUNT(*) FROM collection_txn_mapping WHERE collection_id = :collectionId")
    fun getTransactionCountForCollection(collectionId: Long): Int

    @Query("SELECT * FROM collection_item_exclusions WHERE collection_id = :collectionId")
    fun getExcludedItemsFlow(collectionId: Long): Flow<List<CollectionItemExclusionEntity>>

    @Query("SELECT * FROM collection_item_exclusions WHERE collection_id = :collectionId")
    suspend fun getExcludedItems(collectionId: Long): List<CollectionItemExclusionEntity>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExclusion(exclusion: CollectionItemExclusionEntity)

    @Delete
    suspend fun deleteExclusion(exclusion: CollectionItemExclusionEntity)

    @Query("DELETE FROM collection_item_exclusions WHERE collection_id = :collectionId AND item_id = :itemId")
    suspend fun deleteExclusionByIds(collectionId: Long, itemId: Long)
}