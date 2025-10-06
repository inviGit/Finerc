package com.invi.finerc.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.invi.finerc.common.BankType
import com.invi.finerc.common.PdfTableReader
import com.invi.finerc.common.helper.parseOrderItemsFromExcel
import com.invi.finerc.data.entity.TransactionEntity
import com.invi.finerc.domain.models.TransactionUiModel
import com.invi.finerc.domain.models.TransactionsUiState
import com.invi.finerc.service.TransactionService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionService: TransactionService,
    @ApplicationContext private val context: Context
) : ViewModel()
{
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    private val _transaction = MutableStateFlow<TransactionUiModel?>(null)
    val transaction: StateFlow<TransactionUiModel?> = _transaction.asStateFlow()
    private val _pdfProcessingState = MutableStateFlow<PdfProcessingState>(PdfProcessingState.Idle)
    val pdfProcessingState: StateFlow<PdfProcessingState> = _pdfProcessingState.asStateFlow()
    private var pdfProcessingJob: Job? = null
    private var isCancellationRequested = false

    private val _uiState = MutableStateFlow<TransactionsUiState>(TransactionsUiState(isLoading = true))
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            transactionService.getTransactionsFlow()
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { transactions ->
                    _uiState.update { it.copy(
                        transactions = transactions,
                        isLoading = false,
                        error = null
                    )}
                }
        }
    }

    fun loadTransaction(id: Long) {
        viewModelScope.launch {
            val result = transactionService.getTransaction(id)
            _transaction.value = result
        }
    }

    suspend fun getTransactionsByPlace(place: String): List<TransactionUiModel> {
        return transactionService.getTransactionsByPlace(place)
    }

    suspend fun getTransactions(): List<TransactionUiModel> {
        return transactionService.getAllTransactions()
    }

    fun getTransaction(id: Long, onResult: (TransactionUiModel?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val transaction = transactionService.getTransaction(id)
            withContext(Dispatchers.Main) {
                onResult(transaction)
            }
        }
    }

    fun saveTransactions(
        transactions: List<TransactionEntity>,
        onComplete: ((success: Boolean, count: Int) -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _saveState.value = SaveState.Saving

            try {
                transactionService.saveTransactions(transactions)

                Log.d(
                    "TransactionViewModel", "Successfully saved ${transactions.size} transactions"
                )

                _saveState.value = SaveState.Success(
                    message = "Successfully saved ${transactions.size} transactions"
                )

                withContext(Dispatchers.Main) {
                    onComplete?.invoke(true, transactions.size)
                }

            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error saving transactions", e)

                _saveState.value = SaveState.Error(e.message ?: "Unknown error")

                withContext(Dispatchers.Main) {
                    onComplete?.invoke(false, 0)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTransaction(
        id: Long, onComplete: ((success: Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                transactionService.deleteTransaction(id)
                Log.d("TransactionViewModel", "Successfully deleted transaction")
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(true)
                }
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error deleting transaction", e)
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(false)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("TransactionViewModel", "ViewModel cleared")
    }

    /**
     * Process PDF in background - survives screen changes
     * Uses NonCancellable to prevent cancellation during critical operations
     * Supports bank detection on extracted combined text
     */
    fun processPdfStatement(uri: Uri, password: String?) {
        // Cancel any existing processing if user starts new one
        if (pdfProcessingJob?.isActive == true) {
            isCancellationRequested = true
            pdfProcessingJob?.cancel()
        }

        isCancellationRequested = false

        pdfProcessingJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    _pdfProcessingState.value =
                        PdfProcessingState.Processing("Starting PDF processing...")
                    _isLoading.value = true
                }

                withContext(NonCancellable + Dispatchers.IO) {
                    val pdfTableReader =
                        PdfTableReader(context) // New PDF text/table reader (no OCR)

                    // Step 1: Extract pages' text tables
                    val extractionResult = pdfTableReader.readPdfTables(
                        pdfInputStream = context.contentResolver.openInputStream(uri)!!,
                        password = password,
                        onProgress = { progress ->
                            if (isCancellationRequested) throw CancellationException("User cancelled processing")

                            viewModelScope.launch(Dispatchers.Main) {
                                _pdfProcessingState.value = PdfProcessingState.Processing(progress)
                            }
                        })

                    if (extractionResult.isFailure) {
                        throw extractionResult.exceptionOrNull()!!
                    }

                    val pageTexts = extractionResult.getOrNull()!!
                    val combinedText = pageTexts.joinToString(separator = "\n")

                    withContext(Dispatchers.Main) {
                        _pdfProcessingState.value =
                            PdfProcessingState.Processing("Detected ${pageTexts.size} pages of text")
                    }

                    // Step 2: Detect bank from combined text
                    val bankType = BankType.detectFromText(combinedText)

                    withContext(Dispatchers.Main) {
                        _pdfProcessingState.value =
                            PdfProcessingState.Processing("Detected bank: ${bankType.name}")
                    }

                    // Step 3: Parse extracted text based on bank type
                    val statement = when (bankType) {
                        BankType.AXIS_BANK -> pdfTableReader.parseAxisBankStatement(combinedText)
                        BankType.HDFC_BANK -> pdfTableReader.parseHdfcBankStatement(combinedText)
                        BankType.ICICI_BANK -> pdfTableReader.parseIciciBankStatement(combinedText)
                        BankType.SBI_BANK -> pdfTableReader.parseSbiBankStatement(combinedText)
                        BankType.UNKNOWN -> throw Exception("Unable to detect bank from statement")
                    }

                    withContext(Dispatchers.Main) {
                        _pdfProcessingState.value = PdfProcessingState.Processing(
                            "Parsing completed - ${statement.cyclesWithTransactions[0].transactions.size} transactions found"
                        )
                    }

                    // Step 4: Save transactions in non-cancellable context
                    try {
                        saveTransactions(statement.cyclesWithTransactions[0].transactions)
                        withContext(Dispatchers.Main) {
                            _pdfProcessingState.value = PdfProcessingState.Success(
                                "✅ Successfully processed and saved ${statement.cyclesWithTransactions[0].transactions.size} transactions"
                            )
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            _pdfProcessingState.value =
                                PdfProcessingState.Error("⚠️ PDF processed but failed to save: ${e.message}")
                        }
                    }
                }

            } catch (e: CancellationException) {
                withContext(Dispatchers.Main) {
                    if (isCancellationRequested) _pdfProcessingState.value =
                        PdfProcessingState.Cancelled
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _pdfProcessingState.value = PdfProcessingState.Error("❌ Error: ${e.message}")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                }
            }
        }
    }

    /**
     * Cancel ongoing PDF processing
     */
    fun cancelPdfProcessing() {
        isCancellationRequested = true
        pdfProcessingJob?.cancel()
        viewModelScope.launch(Dispatchers.Main) {
            _pdfProcessingState.value = PdfProcessingState.Cancelled
            _isLoading.value = false
        }
        Log.d("TransactionViewModel", "PDF processing cancellation requested")
    }

    /**
     * Reset PDF processing state
     */
    fun resetPdfProcessingState() {
        _pdfProcessingState.value = PdfProcessingState.Idle
    }

    sealed class SaveState {
        object Idle : SaveState()
        object Saving : SaveState()
        data class Success(val message: String) : SaveState()
        data class Error(val message: String) : SaveState()
    }

    sealed class PdfProcessingState {
        object Idle : PdfProcessingState()
        data class Processing(val progress: String) : PdfProcessingState()
        data class Success(val message: String) : PdfProcessingState()
        data class Error(val message: String) : PdfProcessingState()
        object Cancelled : PdfProcessingState()
    }


    // Excel Processing
    sealed class ExcelProcessingState {
        object Idle : ExcelProcessingState()
        data class Processing(val progress: String) : ExcelProcessingState()
        data class Success(val message: String) : ExcelProcessingState()
        data class Error(val message: String) : ExcelProcessingState()
    }

    val _excelProcessingState = MutableStateFlow<ExcelProcessingState>(ExcelProcessingState.Idle)
    val excelProcessingState: StateFlow<ExcelProcessingState> = _excelProcessingState.asStateFlow()

    fun processExcelFile(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    _excelProcessingState.value = ExcelProcessingState.Processing("Reading Excel file...")
                    _isLoading.value = true
                }

                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Failed to open Excel file")

                val orderItems = parseOrderItemsFromExcel(inputStream)

                withContext(Dispatchers.Main) {
                    _excelProcessingState.value =
                        ExcelProcessingState.Processing("Linking orders to transactions...")
                }

                // Link orders to transactions and save items
                // Assuming you have transactionService.linkOrderItemsToTransactions method
                transactionService.linkOrderItemsToTransactions(orderItems)

                withContext(Dispatchers.Main) {
                    _excelProcessingState.value =
                        ExcelProcessingState.Success("Successfully processed ${orderItems.size} order items.")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _excelProcessingState.value = ExcelProcessingState.Error("Error processing Excel: ${e.message}")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                }
            }
        }
    }

    suspend fun getTransactionItems(transactionId: Long): List<com.invi.finerc.data.entity.TransactionItemEntity> {
        return transactionService.getTransactionItems(transactionId)
    }
}

// Custom exception for user cancellation
class CancellationException(message: String) : Exception(message)