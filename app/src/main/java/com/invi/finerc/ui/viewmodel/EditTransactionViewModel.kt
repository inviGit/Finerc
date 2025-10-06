package com.invi.finerc.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.invi.finerc.data.entity.TransactionEntity
import com.invi.finerc.domain.mapper.TransactionMapper.copyForUpdate
import com.invi.finerc.domain.models.Category
import com.invi.finerc.domain.models.TransactionStatus
import com.invi.finerc.domain.models.TransactionType
import com.invi.finerc.domain.models.TransactionUiModel
import com.invi.finerc.service.TransactionService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class EditTransactionViewModel @Inject constructor(
    private val service: TransactionService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val transactionId: Long = checkNotNull(savedStateHandle["transactionId"]) {
        "transactionId is required"
    }

    private val _transaction = MutableStateFlow<TransactionEntity?>(null)
    val transaction: StateFlow<TransactionEntity?> = _transaction

    init {
        viewModelScope.launch {
            _transaction.value = service.getTransactionEntity(transactionId)
        }
    }

    fun saveTransaction(
        place: String,
        category: Category,
        txnType: TransactionType,
        bankName: String,
        amount: Double,
        txnDate: Long,
        description: String,
        note: String,
        status: TransactionStatus
    ) {
        viewModelScope.launch {
            val current = _transaction.value
            if (current != null) {
                val updated = current.copyForUpdate(
                    place = place,
                    category = category,
                    txnType = txnType,
                    bankName = bankName,
                    amount = amount,
                    txnDate = txnDate,
                    description = description,
                    note = note,
                    status = status
                )
                updateTransaction(updated)
                _transaction.value = updated
            }
        }
    }

    fun updateTransaction(
        updatedFields: TransactionEntity,
        onComplete: ((success: Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                service.updateTransaction(updatedFields)
                Log.d("TransactionViewModel", "Successfully updated transaction")
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(true)
                }
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error updating transaction", e)
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(false)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addTransaction(
        transaction: TransactionUiModel, onComplete: ((success: Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                service.addTransaction(transaction)
                Log.d("TransactionViewModel", "Successfully added transaction")
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(true)
                }
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error adding transaction", e)
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(false)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
