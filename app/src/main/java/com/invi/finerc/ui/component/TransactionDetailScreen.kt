package com.invi.finerc.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.invi.finerc.common.AppUtils
import com.invi.finerc.domain.models.Category
import com.invi.finerc.domain.models.TransactionItemModel
import com.invi.finerc.domain.models.TransactionType
import com.invi.finerc.domain.models.TransactionUiModel
import com.invi.finerc.ui.screen.getCategoryColors
import com.invi.finerc.ui.viewmodel.TransactionViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.Date
import java.util.Locale


@Composable
fun TransactionDetailScreen(
    transactionId: Long,
    navController: NavHostController,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val transaction by viewModel.transaction.collectAsState()
    val scope = rememberCoroutineScope()

    // Local editable states initialized on transaction change
    var place by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(Category.FOOD) }
    var transactionType by remember { mutableStateOf(TransactionType.DEBIT) }
    var bankName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf(0.0) }
    var body by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(0L) }
    var note by remember { mutableStateOf("") }
    var showBulkDeleteDialog by remember { mutableStateOf(false) }
    var transactionsByPlace by remember { mutableStateOf<List<TransactionUiModel>>(emptyList()) }
    var transactionItems by remember {
        mutableStateOf<List<TransactionItemModel>>(
            emptyList()
        )
    }

    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
        transactionItems = viewModel.getTransactionItems(transactionId)
    }

    LaunchedEffect(transaction) {
        place = transaction?.place.orEmpty()
        category = transaction?.category ?: Category.FOOD
        transactionType = transaction?.txnType ?: TransactionType.DEBIT
        bankName = transaction?.bankName.orEmpty()
        amount = transaction?.amount ?: 0.0
        date = transaction?.txnDate ?: 0L
        body = transaction?.description ?: ""
        note = transaction?.note.orEmpty()

        if (place.isNotEmpty()) {
            scope.launch {
                transactionsByPlace = viewModel.getTransactionsByPlace(place)
            }
        }
    }

    if (transaction == null) {
        Text("Loading...", modifier = Modifier.padding(16.dp))
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0A))
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TransactionDetailHeaderSection(
                category = category,
                amount = amount.toString(),
                place = place,
                date = date,
                isLoading = false
            )

            // Bank and Expense Toggle Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = bankName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    val isExpense = transactionType.name == "SENT"
                    Card(
                        shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(
                            containerColor = if (isExpense) Color(0xFFEF4444) else Color(0xFF00D4AA)
                        )
                    ) {
                        Text(
                            text = if (isExpense) "Expense" else "Income",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // SMS Body Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "SMS Message",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = body, style = MaterialTheme.typography.bodyMedium, color = Color.Gray
                    )
                    if (note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Notes",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Chart Section: Transactions by Place
            if (transactionsByPlace.isNotEmpty()) {
                val filterOptions = listOf("1M", "1Y")
                var selectedFilter by remember { mutableStateOf(0) }
                val now = System.currentTimeMillis()
                val zone = ZoneId.systemDefault()

                val filteredChartData = remember(transactionsByPlace, selectedFilter, now) {
                    val filterMillis = when (selectedFilter) {
                        0 -> AppUtils.cutoffMillis(
                            AppUtils.CutoffKind.MonthFromDay(1), now, zone
                        )         // 1M: from 1st of month
                        1 -> AppUtils.cutoffMillis(
                            AppUtils.CutoffKind.YearFromJan1, now, zone
                        )            // 1Y: from Jan 1
                        else -> Long.MAX_VALUE
                    }

                    transactionsByPlace.filter { it.txnDate >= filterMillis }.groupBy { it.txnDate }
                        .map { (date, txns) ->
                            SimpleDateFormat(
                                "dd MMM", Locale.getDefault()
                            ).format(Date(date)) to txns.sumOf { it.amount }
                        }
                }

                MinimalBarChartSynced(
                    heading = "Spending by Visit",
                    data = filteredChartData,
                    onFilterChange = { selectedFilter = (selectedFilter + 1) % filterOptions.size },
                    filterOptions = filterOptions,
                    selectedFilter = selectedFilter,
                    modifier = Modifier
                        .width(380.dp)
                        .height(220.dp)
                )
            }

            // Edit and Delete Buttons Row
            val editGradient = Brush.linearGradient(listOf(Color(0xFF00D4AA), Color(0xFF00B894)))
            val deleteGradient = Brush.linearGradient(listOf(Color(0xFFEB5757), Color(0xFFFFA857)))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        navController.navigate("editTransaction?transactionId=$transactionId")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .background(editGradient, shape = RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text("Edit", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (transactionId != 0L) {
                            scope.launch {
                                showBulkDeleteDialog = true
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .background(deleteGradient, shape = RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // Transaction Items Card
            if (transactionItems.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Items in this transaction",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Spacer(Modifier.height(8.dp))
                        transactionItems.forEach { item ->
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(
                                    "Product: ${item.productName}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Order ID: ${item.orderId}", color = Color.Gray)
                                Text("Quantity: ${item.quantity}", color = Color.Gray)
                                Text("Unit Price: ₹${item.unitPrice}", color = Color.Gray)
                                if (item.returnAmount > 0) {
                                    Text(
                                        "Returned: ₹${item.returnAmount}",
                                        color = Color(0xFFEF4444)
                                    )
                                }
                            }
                            Divider(color = Color.White.copy(alpha = 0.1f))
                        }
                    }
                }
            }
        }
    }

    if (showBulkDeleteDialog) {
        BulkDeleteDialog(
            showDialog = showBulkDeleteDialog,
            onDismiss = { showBulkDeleteDialog = false },
            originalPlace = place,
            originalCategory = category,
            currentId = transactionId,
            transactions = transactionsByPlace,
            onDeleteCurrentOnly = {
                scope.launch {
                    try {
                        if (transactionId != 0L) {
                            scope.launch {
                                viewModel.deleteTransaction(transactionId)
                                // Perform UI update or navigation after deletion
                            }
                        }
                        showBulkDeleteDialog = false
                        navController.popBackStack()
                    } catch (_: Exception) {
                    }
                }
            },
            onConfirmDelete = { ids ->
                scope.launch {
                    try {
                        ids.forEach { id ->
                            scope.launch {
                                viewModel.deleteTransaction(id)
                            }
                        }
                        showBulkDeleteDialog = false
                        navController.popBackStack()
                    } catch (_: Exception) {
                    }
                }
            })
    }
}

@Composable
fun TransactionDetailHeaderSection(
    category: Category, amount: String, place: String, date: Long, isLoading: Boolean
) {
    val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val categoryColors = getCategoryColors(category)
    val categoryIcon = category.icon

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(categoryColors), shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            text = place,
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = category.name,
                        tint = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(56.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = amount,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dateFormat.format(Date(date)),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}