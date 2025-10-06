package com.invi.finerc.domain.mapper

import com.invi.finerc.data.entity.CollectionWithStatsEntity
import com.invi.finerc.data.entity.CollectionWithTransactionsEntity
import com.invi.finerc.domain.models.CollectionUiModel


object CollectionMapper {

    fun entityToUiModel(entity: CollectionWithStatsEntity) = CollectionUiModel (
        id = entity.collection.id,
        name = entity.collection.name,
        transactionCount = entity.transactionCount,
        totalSpent = entity.totalAmount
    )

    fun entityToUiModel(entity: CollectionWithTransactionsEntity) = CollectionUiModel (
        id = entity.collection.id,
        name = entity.collection.name,
        transactionCount = entity.transactions.size,
        totalSpent = entity.transactions.sumOf { it.amount },
        transactions = entity.transactions.map { TransactionMapper.entityToUiModel(it) }
    )
}
