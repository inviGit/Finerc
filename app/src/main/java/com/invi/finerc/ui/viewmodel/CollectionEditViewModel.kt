package com.invi.finerc.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.invi.finerc.data.entity.CollectionItemExclusionEntity
import com.invi.finerc.domain.models.CollectionModel
import com.invi.finerc.service.CollectionService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionEditViewModel @Inject constructor(
    private val service: CollectionService, savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val collectionId: Long = savedStateHandle.get<Long>("collectionId") ?: 0L

    private val _collection = MutableStateFlow<CollectionModel?>(null)
    val collection: StateFlow<CollectionModel?> = _collection.asStateFlow()

    private val _excludedItems = MutableStateFlow<Set<Long>>(emptySet())
    val excludedItems: StateFlow<Set<Long>> = _excludedItems.asStateFlow()

    private val _collectionName = MutableStateFlow("")
    val collectionName: StateFlow<String> = _collectionName.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadCollection()
    }

    private fun loadCollection() {
        viewModelScope.launch {
            _isLoading.value = true

            launch {
                service.getCollectionWithTransactionsFlow(collectionId).collect {
                    _collection.value = it
                    _collectionName.value = it.name
                }
            }

            launch {
                service.getExcludedItemsFlow(collectionId).collect { exclusions ->
                    _excludedItems.value = exclusions.map { it.itemId }.toSet()
                }
            }

            _isLoading.value = false
        }
    }

    fun updateCollectionName(name: String) {
        _collectionName.value = name
    }

    fun toggleItemExclusion(itemId: Long, isExcluded: Boolean) {
        viewModelScope.launch {
            if (isExcluded) {
                service.insertExclusion(
                    CollectionItemExclusionEntity(
                        collectionId = collectionId, itemId = itemId
                    )
                )
            } else {
                service.deleteExclusionByIds(collectionId, itemId)
            }
        }
    }

    fun removeTransaction(transactionId: Long) {
        viewModelScope.launch {
            service.removeTransactionFromCollection(collectionId, transactionId)
        }
    }

    fun saveCollection() {
        viewModelScope.launch {
            _collection.value?.let {
                service.updateCollection(it.id, it.name)
            }
        }
    }
}
