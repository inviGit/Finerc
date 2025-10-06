package com.invi.finerc.domain.mapper

import com.invi.finerc.common.AppUtils
import com.invi.finerc.data.entity.TransactionEntity
import com.invi.finerc.data.entity.TransactionItemEntity
import com.invi.finerc.domain.models.Category
import com.invi.finerc.domain.models.CurrencyType
import com.invi.finerc.domain.models.TransactionSource
import com.invi.finerc.domain.models.TransactionStatus
import com.invi.finerc.domain.models.TransactionType
import com.invi.finerc.domain.models.TransactionUiModel

object TransactionMapper {
    fun entityToUiModel(entity: TransactionEntity) = TransactionUiModel(
            id = entity.id,
            source = entity.source ?: TransactionSource.UN_ASSIGNED,
            txnType = entity.txnType ?: TransactionType.UN_ASSIGNED,
            amount = entity.amount,
            txnDate = entity.txnDate,
            bankName = entity.bankName.orEmpty(),
            category = entity.category ?: Category.OTHERS,
            place = entity.place.orEmpty(),
            description = entity.description.orEmpty(),
            note = entity.note.orEmpty(),
            currencyCode = entity.currencyCode ?: CurrencyType.INR,
            status = entity.status ?: TransactionStatus.ACTIVE
        )

    fun uiModelToEntity(transactionUiModel: TransactionUiModel): TransactionEntity {
        val transactionId = AppUtils.generateTransactionId(
            transactionUiModel.txnDate,
            transactionUiModel.bankName,
            transactionUiModel.amount,
            transactionUiModel.txnType ?: TransactionType.DRAFT,
            transactionUiModel.description ?: "")

        return TransactionEntity(
            id = transactionUiModel.id ?: 0L,
            transactionId = transactionId,
            source = transactionUiModel.source,
            bankName = transactionUiModel.bankName,
            category = transactionUiModel.category,
            place = transactionUiModel.place,
            txnDate = transactionUiModel.txnDate,
            amount = transactionUiModel.amount,
            txnType = transactionUiModel.txnType,
            description = transactionUiModel.description,
            currencyCode = transactionUiModel.currencyCode,
            note = transactionUiModel.note ?: "",
            status = transactionUiModel.status
        )
    }

    fun TransactionEntity.copyForUpdate(
        place: String? = null,
        category: Category? = null,
        txnType: TransactionType? = null,
        bankName: String? = null,
        amount: Double? = null,
        txnDate: Long? = null,
        description: String? = null,
        note: String? = null,
        status: TransactionStatus? = null,
    ): TransactionEntity {
        return this.copy(
            place = place ?: this.place,
            category = category ?: this.category,
            txnType = txnType ?: this.txnType,
            bankName = bankName ?: this.bankName,
            amount = amount ?: this.amount,
            txnDate = txnDate ?: this.txnDate,
            description = description ?: this.description,
            note = note ?: this.note,
            status = status ?: this.status
        )
    }

    fun entityToUiModelWithItems(entity: TransactionEntity,
                         items: List<TransactionItemEntity> = emptyList()
    ) = TransactionUiModel(
        id = entity.id,
        source = entity.source ?: TransactionSource.UN_ASSIGNED,
        txnType = entity.txnType ?: TransactionType.UN_ASSIGNED,
        amount = entity.amount,
        txnDate = entity.txnDate,
        bankName = entity.bankName.orEmpty(),
        category = entity.category ?: Category.OTHERS,
        place = entity.place.orEmpty(),
        description = entity.description.orEmpty(),
        note = entity.note.orEmpty(),
        currencyCode = entity.currencyCode ?: CurrencyType.INR,
        status = entity.status ?: TransactionStatus.ACTIVE,
        transactionItem = items
    )

}