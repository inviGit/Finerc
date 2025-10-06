package com.invi.finerc.ui.screen

import androidx.navigation.NavController
import com.invi.finerc.ui.component.EditableTransactionCard
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.invi.finerc.domain.mapper.TransactionMapper
import com.invi.finerc.domain.models.Category
import com.invi.finerc.ui.viewmodel.CollectionEditViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionEditScreen(
    collectionId: Long,
    navController: NavController?,
    viewModel: CollectionEditViewModel = hiltViewModel()
) {
    val collection by viewModel.collection.collectAsState()
    val collectionName by viewModel.collectionName.collectAsState()
    val excludedItems by viewModel.excludedItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showRemoveDialog by remember { mutableStateOf(false) }
    var transactionToRemove by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Collection") },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.saveCollection()
                        navController?.popBackStack()
                    }) {
                        Icon(Icons.Default.Check, "Save", tint = Color(0xFF00D4AA))
                    }
                }
            )
        }
    ) { paddingValues ->
        if (collection == null || isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Editable Collection Name
            OutlinedTextField(
                value = collectionName,
                onValueChange = { viewModel.updateCollectionName(it) },
                label = { Text("Collection Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00D4AA),
                    focusedLabelColor = Color(0xFF00D4AA)
                )
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = Color.Gray.copy(alpha = 0.3f)
            )

            // Transactions List
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                collection?.transactions?.let { transactions ->
                    items(transactions) { transaction ->
                        EditableTransactionCard(
                            transaction = transaction,
                            excludedItems = excludedItems,
                            onItemToggle = { itemId, isExcluded ->
                                viewModel.toggleItemExclusion(itemId, isExcluded)
                            },
                            onRemoveTransaction = {
                                transactionToRemove = transaction.id
                                showRemoveDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Remove Transaction") },
            text = { Text("Are you sure you want to remove this transaction from the collection?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        transactionToRemove?.let { viewModel.removeTransaction(it) }
                        showRemoveDialog = false
                        transactionToRemove = null
                    }
                ) {
                    Text("Remove", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
