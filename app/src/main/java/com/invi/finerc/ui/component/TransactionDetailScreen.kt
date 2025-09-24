package com.invi.finerc.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.invi.finerc.getCategoryColors
import com.invi.finerc.models.Category
import com.invi.finerc.models.SMSType
import com.invi.finerc.models.SmsMessageModel
import com.invi.finerc.transaction.SmsRepository
import com.invi.finerc.transaction.SmsTransactionDatabase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.LocalDate


@Composable
fun TransactionDetailScreen(
    transactionId: Long, navController: NavHostController
) {
    val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    val context = LocalContext.current
    val db = remember { SmsTransactionDatabase.getInstance(context) }
    val repo = remember { SmsRepository(db.smsTransactionDao(), db.collectionDao()) }
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(transactionId != 0L) }
    var error by remember { mutableStateOf<String?>(null) }

    // State for all fields
    var place by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(Category.FOOD) }
    var transactionType by remember { mutableStateOf(SMSType.SENT) }
    var bankName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var groupedData by remember { mutableStateOf<List<Pair<String, Double>>>(emptyList()) }
    var note by remember { mutableStateOf("") }
    var showBulkDeleteDialog by remember { mutableStateOf(false) }

    // Load transaction if editing
    LaunchedEffect(transactionId) {
        if (transactionId != 0L) {
            isLoading = true
            try {
                val msg = repo.getMessage(transactionId)
                if (msg != null) {

                    place = msg.place
                    category = msg.category
                    transactionType = msg.transactionType
                    bankName = msg.bankName
                    amount = msg.amount.toString()
                    date = msg.date
                    body =  msg.body
                    note = msg.note

                    if (place.isNotEmpty()) {
                        val transactions = repo.getTransactionsByPlace(place)
                        groupedData = transactions
                            .groupBy { it.date }
                            .map { (date, txns) ->
                                dateFormat.format(Date(date)) to txns.sumOf { it.amount }
                            }
                    }

                } else {
                    error = "Transaction not found"
                }
            } catch (e: Exception) {
                error = e.message
            }
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .padding(16.dp)
        .verticalScroll
            (rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        TransactionDetailHeaderSection(
            category,
            amount = amount,
            place = place,
            date = date,
            isLoading = false
        )

        // 2. Bank and Expense Toggle Card
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
                // Expense/Income Toggle
                val isExpense = transactionType.name == "SENT"
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
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

        // 3. SMS Body Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "SMS Message",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
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
        if (groupedData.isNotEmpty()) {
            val filterOptions = listOf("1M", "1Y")
            var selectedFilter by remember { mutableStateOf(0) }

            // Filtered chart data
            val now = System.currentTimeMillis()
            val filteredChartData = remember(groupedData, selectedFilter, now) {
                val filterMillis = when (selectedFilter) {
                    0 -> 31L * 24 * 60 * 60 * 1000 // 1M
                    1 -> 365L * 24 * 60 * 60 * 1000 // 1Y
                    else -> Long.MAX_VALUE
                }
                val cutoff = now - filterMillis
                groupedData.filter { (label, _) ->
                    // Try to parse label as date, fallback to include all
                    try {
                        val parsedDate = dateFormat.parse(label)?.time ?: now
                        parsedDate >= cutoff
                    } catch (e: Exception) {
                        true
                    }
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

//         4. Edit Transaction Option
        // Gradient colors for buttons
        val editGradient = Brush.linearGradient(listOf(
            Color(0xFF00D4AA),
            Color(0xFF00B894)
        ))
        val deleteGradient = Brush.linearGradient(listOf(Color(0xFFEB5757), Color(0xFFFFA857))) // Red to orange

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    navController?.navigate("editTransaction?transactionId=$transactionId")
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
                            // Find similar by place and category
                            val similar = repo.getTransactionsByPlace(place)
                                .filter { it.category == category && it.id != null && it.id != transactionId }
                            if (similar.isEmpty()) {
                                // Direct delete if no similar
                                repo.deleteMessage(transactionId)
                                navController?.popBackStack()
                            } else {
                                showBulkDeleteDialog = true
                            }
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

    }

    if (showBulkDeleteDialog && place.isNotBlank()) {
        BulkDeleteDialog(
            showDialog = showBulkDeleteDialog,
            onDismiss = { showBulkDeleteDialog = false },
            originalPlace = place,
            originalCategory = category,
            currentId = transactionId,
            onDeleteCurrentOnly = {
                scope.launch {
                    try {
                        if (transactionId != 0L) {
                            repo.deleteMessage(transactionId)
                        }
                        showBulkDeleteDialog = false
                        navController?.popBackStack()
                    } catch (_: Exception) {}
                }
            },
            onConfirmDelete = { ids ->
                scope.launch {
                    try {
                        ids.forEach { id ->
                            repo.deleteMessage(id)
                        }
                        showBulkDeleteDialog = false
                        navController?.popBackStack()
                    } catch (e: Exception) {
                        // no-op: you could surface error state if desired
                    }
                }
            }
        )
    }
}

@Composable
fun BulkDeleteDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    originalPlace: String,
    originalCategory: Category,
    currentId: Long,
    onDeleteCurrentOnly: () -> Unit,
    onConfirmDelete: (Set<Long>) -> Unit
) {
    val context = LocalContext.current
    val db = remember { SmsTransactionDatabase.getInstance(context) }
    val repo = remember { SmsRepository(db.smsTransactionDao(), db.collectionDao()) }
    val scope = rememberCoroutineScope()

    var transactions by remember { mutableStateOf<List<SmsMessageModel>>(emptyList()) }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var isLoading by remember { mutableStateOf(false) }

    fun reload() {
        scope.launch {
            isLoading = true
            try {
                val txns = repo.getTransactionsByPlace(originalPlace)
                    .filter { it.category == originalCategory }
                transactions = txns
                selectedIds = txns.mapNotNull { it.id }.toSet()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(showDialog) { if (showDialog) reload() }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = { onConfirmDelete(selectedIds) }, enabled = selectedIds.isNotEmpty()) {
                    Text("Delete ${selectedIds.size}")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDeleteCurrentOnly) { Text("Delete current only") }
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                }
            },
            title = { Text("Bulk Delete Similar Transactions", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (transactions.isEmpty()) {
                        Text("No similar transactions found for the selected period.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            transactions.forEach { txn ->
                                val checked = selectedIds.contains(txn.id)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedIds = if (checked) selectedIds - txn.id!! else selectedIds + txn.id!!
                                        },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(txn.place)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(txn.date)))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        androidx.compose.material3.Checkbox(checked = checked, onCheckedChange = { c ->
                                            selectedIds = if (c) selectedIds + txn.id!! else selectedIds - txn.id!!
                                        })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun TransactionDetailHeaderSection(
    category: Category,
    amount: String,
    place: String,
    date: Long,
    isLoading: Boolean
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
                    brush = Brush.linearGradient(categoryColors),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
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


//                    StatItem(
//                        label = "Transactions",
//                        value = place,
//                        icon = Icons.Default.Receipt
//                    )
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun TransactionDetailScreenPreview() {
    androidx.compose.material3.MaterialTheme {
        TransactionDetailScreen(
            transactionId = 12L,
            navController = androidx.navigation.compose.rememberNavController()
        )
    }
}