package com.invi.finerc.domain.mapper

import com.invi.finerc.data.entity.CollectionWithStatsEntity
import com.invi.finerc.data.entity.CollectionWithTransactionsEntity
import com.invi.finerc.domain.models.CollectionModel


object CollectionMapper {

    fun entityToUiModel(entity: CollectionWithStatsEntity) = CollectionModel(
        id = entity.collection.id,
        name = entity.collection.name,
        transactionCount = entity.transactionCount,
        totalTransactionAmount = entity.totalAmount
    )

    fun entityToUiModel(entity: CollectionWithTransactionsEntity) = CollectionModel(
        id = entity.collection.id,
        name = entity.collection.name,
        transactionCount = entity.transactions.size,
        totalTransactionAmount = entity.transactions.sumOf { it.amount },
        transactions = entity.transactions.map { TransactionMapper.entityToUiModel(it) }
    )
}
