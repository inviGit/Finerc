package com.invi.finerc.service

import com.invi.finerc.data.entity.CollectionEntity
import com.invi.finerc.data.entity.CollectionItemExclusionEntity
import com.invi.finerc.data.entity.CollectionSmsMappingEntity
import com.invi.finerc.data.entity.CollectionWithStatsEntity
import com.invi.finerc.data.entity.CollectionWithTransactionsEntity
import com.invi.finerc.data.repository.TransactionRepository
import com.invi.finerc.domain.mapper.CollectionMapper
import com.invi.finerc.domain.mapper.TransactionMapper
import com.invi.finerc.domain.models.CollectionUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionService @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    private val entityStateCache = mutableMapOf<Long, CollectionWithStatsEntity>()

    suspend fun createCollection(name: String): Long = transactionRepository.createCollection(name)

    fun getCollectionsWithStatsFlow(): Flow<List<CollectionUiModel>> =
        transactionRepository.getCollectionsWithStatsFlow().map { list ->
            list.forEach { entityStateCache[it.collection.id] = it }
            list.map { CollectionMapper.entityToUiModel(it) }
        }

    fun getCollectionWithTransactionsFlow(collectionId: Long): Flow<CollectionUiModel> {
        return transactionRepository.getCollectionWithTransactionsFlow(collectionId).map { entity ->
            val transactionsWithItems = entity.transactions.map { transaction ->
                val items = transactionRepository.getTransactionItems(transaction.id)
                TransactionMapper.entityToUiModelWithItems(transaction, items)
            }

            CollectionUiModel(
                id = entity.collection.id,
                name = entity.collection.name,
                transactionCount = entity.transactions.size,
                totalSpent = entity.transactions.sumOf { it.amount },
                transactions = transactionsWithItems
            )
        }
    }

    suspend fun addTransactionsToCollection(collectionSmsMappingEntityList: List<CollectionSmsMappingEntity>) {
        transactionRepository.addTransactionsToCollection(collectionSmsMappingEntityList)
    }

    fun getExcludedItemsFlow(collectionId: Long): Flow<List<CollectionItemExclusionEntity>> {
        return transactionRepository.getExcludedItemsFlow(collectionId)
    }

    suspend fun insertExclusion(exclusion: CollectionItemExclusionEntity) {
        return transactionRepository.insertExclusion(exclusion)
    }

    suspend fun deleteExclusion(exclusion: CollectionItemExclusionEntity) {
        return transactionRepository.deleteExclusion(exclusion)
    }

    suspend fun deleteExclusionByIds(collectionId: Long, itemId: Long) {
        return transactionRepository.deleteExclusionByIds(collectionId, itemId)
    }

    suspend fun removeTransactionFromCollection(collectionId: Long, transactionId: Long) {
        transactionRepository.removeTransactionFromCollection(collectionId, transactionId)
    }

    suspend fun updateCollection(id: Long, name: String) {
        transactionRepository.updateCollection(id, name)
    }

}
