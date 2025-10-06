package com.invi.finerc.ui.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.invi.finerc.domain.models.CollectionUiModel
import com.invi.finerc.service.CollectionService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val collectionService: CollectionService
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun createCollection(name: String, afterAdd: (() -> Unit)? = null) {
        viewModelScope.launch {
            collectionService.createCollection(name)
            afterAdd?.invoke()
        }
    }

    // Reactive UI Flow for collection list
    val collections: StateFlow<List<CollectionUiModel>> =
        collectionService.getCollectionsWithStatsFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val palette = listOf(
        Color(0xFF00D4AA),
        Color(0xFF6366F1),
        Color(0xFFEF4444),
        Color(0xFFF59E0B),
        Color(0xFF10B981)
    )

}
