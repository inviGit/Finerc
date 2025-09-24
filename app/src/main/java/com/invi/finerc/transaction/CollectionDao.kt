package com.invi.finerc.transaction

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.invi.finerc.models.CollectionEntity
import com.invi.finerc.models.CollectionSmsMappingEntity
import com.invi.finerc.models.SMSTransactionEntity

@Dao
interface CollectionDao {
    
    // Collection CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMapping(mapping: CollectionSmsMappingEntity)
    
    @Query("DELETE FROM collection_sms_mapping WHERE collection_id = :collectionId AND sms_id = :smsId")
    fun removeMapping(collectionId: Long, smsId: Long): Int

    @Query("DELETE FROM collection_sms_mapping WHERE collection_id = :collectionId")
    fun removeAllMappingsForCollection(collectionId: Long): Int

    @Query("DELETE FROM collection_sms_mapping WHERE sms_id = :smsId")
    fun removeAllMappingsForSms(smsId: Long): Int

    // Query operations
    @Query("SELECT s.* FROM sms_transactions s INNER JOIN collection_sms_mapping m ON s.id = m.sms_id WHERE m.collection_id = :collectionId ORDER BY s.date DESC")
    fun getTransactionsForCollection(collectionId: Long): List<SMSTransactionEntity>

    @Query("SELECT c.* FROM collections c INNER JOIN collection_sms_mapping m ON c.id = m.collection_id WHERE m.sms_id = :smsId")
    fun getCollectionsForSms(smsId: Long): List<CollectionEntity>

    @Query("SELECT COUNT(*) FROM collection_sms_mapping WHERE collection_id = :collectionId")
    fun getTransactionCountForCollection(collectionId: Long): Int
}
