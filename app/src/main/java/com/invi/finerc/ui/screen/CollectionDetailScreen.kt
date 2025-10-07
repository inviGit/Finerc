package com.invi.finerc.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.invi.finerc.data.entity.CollectionSmsMappingEntity
import com.invi.finerc.ui.component.CollectionAddTransactionDialog
import com.invi.finerc.ui.component.DetailedHeader
import com.invi.finerc.ui.component.TransactionCard
import com.invi.finerc.ui.viewmodel.CollectionDetailViewModel
import kotlinx.coroutines.launch

@Composable
fun CollectionDetailScreen(
    collectionId: Long,
    navController: NavController?,
    viewModel: CollectionDetailViewModel = hiltViewModel()
) {
    val collection by viewModel.collectionWithTransaction.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scope = rememberCoroutineScope()

    var showFabMenu by remember { mutableStateOf(false) }
    var showTransactionSelectDialog by remember { mutableStateOf(false) }

    if (collection == null && isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val transactions = collection?.transactions ?: emptyList()
    val totalAmount = collection?.transactions
        ?.flatMap { it.transactionItems.orEmpty() }
        ?.sumOf { (it.unitPrice + it.unitPriceTax) * it.quantity }
        ?: 0.0
    val txnCount = collection?.transactionCount ?: 0

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)) {
            // Header with padding
            DetailedHeader(
                totalAmount = totalAmount,
                messageCount = txnCount,
                isLoading = isLoading
            )

            // Transaction list
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 88.dp // Extra padding for FAB
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(transactions) { txn ->
                    TransactionCard(
                        message = txn,
                        items = txn.transactionItems.orEmpty(),
                        onClick = {
                            navController?.navigate("transactionDetail?transactionId=${txn.id}")
                        }
                    )
                }
            }
        }

        // FAB menu with proper spacing
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = if (showFabMenu) 80.dp else 0.dp)
            ) {
                if (showFabMenu) {
                    Button(
                        onClick = {
                            showFabMenu = false
                            // TODO: Handle add new transaction
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        )
                    ) {
                        Text("Add New Transaction", color = Color.Black)
                    }

                    Button(
                        onClick = {
                            showFabMenu = false
                            showTransactionSelectDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        )
                    ) {
                        Text("Add Existing Transaction", color = Color.Black)
                    }
                }

                FloatingActionButton(
                    onClick = { showFabMenu = !showFabMenu },
                    containerColor = Color(0xFF00D4AA)
                ) {
                    Icon(
                        imageVector = if (showFabMenu) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Add Menu",
                        tint = Color.Black
                    )
                }
            }
        }
    }

    if (showTransactionSelectDialog) {
        CollectionAddTransactionDialog(
            showDialog = showTransactionSelectDialog,
            onDismiss = { showTransactionSelectDialog = false },
            onConfirm = { ids ->
                scope.launch {
                    try {
                        val collectionSmsMappingEntityList = ids.map { txnId ->
                            CollectionSmsMappingEntity(
                                collectionId = collectionId,
                                smsId = txnId
                            )
                        }
                        viewModel.addTransactionsToCollection(collectionSmsMappingEntityList)
                        showTransactionSelectDialog = false
                        navController?.popBackStack()
                    } catch (_: Exception) {
                        // Handle error
                    }
                }
            },
            collectionId = collectionId
        )
    }
}
