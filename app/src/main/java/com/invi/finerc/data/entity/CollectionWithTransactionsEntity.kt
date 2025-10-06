package com.invi.finerc.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class CollectionWithTransactionsEntity(
    @Embedded val collection: CollectionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = CollectionSmsMappingEntity::class,
            parentColumn = "collection_id",
            entityColumn = "txn_id"
        )
    )
    val transactions: List<TransactionEntity>
)