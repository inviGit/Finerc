package com.invi.finerc.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "collection_sms_mapping",
    primaryKeys = ["collection_id", "sms_id"],
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collection_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SMSTransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sms_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["collection_id"]),
        Index(value = ["sms_id"])
    ]
)
data class CollectionSmsMappingEntity(
    @ColumnInfo(name = "collection_id") val collectionId: Long,
    @ColumnInfo(name = "sms_id") val smsId: Long
)
