package com.invi.finerc.data.entity

import androidx.room.Embedded

data class CollectionWithStatsEntity(
    @Embedded val collection: CollectionEntity,
    val transactionCount: Int,
    val totalAmount: Double
)