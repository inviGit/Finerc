package com.invi.finerc.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.invi.finerc.data.entity.CollectionSmsMappingEntity
import com.invi.finerc.domain.models.CollectionModel
import com.invi.finerc.service.CollectionService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CollectionDetailViewModel @Inject constructor(
    private val collectionService: CollectionService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val collectionId: Long = checkNotNull(savedStateHandle["collectionId"]) {
        "collectionId is required"
    }

    val collectionWithTransaction: StateFlow<CollectionModel?> = collectionService
        .getCollectionWithTransactionsFlow(collectionId)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val isLoading: StateFlow<Boolean> = collectionWithTransaction
        .map { it == null }
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    suspend fun addTransactionsToCollection(collectionSmsMappingEntityList: List<CollectionSmsMappingEntity>) {
        collectionService.addTransactionsToCollection(collectionSmsMappingEntityList)
    }
}
