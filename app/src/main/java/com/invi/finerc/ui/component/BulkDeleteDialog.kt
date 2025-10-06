package com.invi.finerc.ui.component


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invi.finerc.common.AppUtils
import com.invi.finerc.domain.models.Category
import com.invi.finerc.domain.models.TransactionType
import com.invi.finerc.domain.models.TransactionUiModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun BulkDeleteDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    originalPlace: String,
    originalCategory: Category,
    currentId: Long,
    transactions: List<TransactionUiModel>,
    onDeleteCurrentOnly: () -> Unit,
    onConfirmDelete: (Set<Long>) -> Unit,
) {
    val scope = rememberCoroutineScope()

    var selectedMonth by remember {
        mutableIntStateOf(
            Calendar.getInstance().get(Calendar.MONTH) + 1
        )
    } // Calendar.MONTH zero-based
    var selectedYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var bulkUpdateTransactions by remember { mutableStateOf<List<TransactionUiModel>>(emptyList()) }
    var bulkUpdateSelected by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var isLoadingTransactions by remember { mutableStateOf(false) }
    val months = AppUtils.months
    val years = (2020..(Calendar.getInstance().get(Calendar.YEAR) + 1)).toList()

    fun reload() {
        scope.launch {
            isLoadingTransactions = true
            try {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, selectedYear)
                    set(Calendar.MONTH, selectedMonth - 1)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val start = cal.timeInMillis
                cal.add(Calendar.MONTH, 1)
                val end = cal.timeInMillis

                val txns =
                    transactions.filter { it.place == originalPlace && it.category == originalCategory }
                        .filter { it.txnDate in start until end }

                bulkUpdateTransactions = txns
                bulkUpdateSelected = txns.mapNotNull { it.id }.toSet()
            } catch (e: Exception) {
                bulkUpdateTransactions = emptyList()
                bulkUpdateSelected = emptySet()
            } finally {
                isLoadingTransactions = false
            }
        }
    }

    LaunchedEffect(showDialog) {
        if (showDialog) reload()
    }

    LaunchedEffect(selectedMonth, selectedYear) {
        if (showDialog) reload()
    }

    if (showDialog) {
        AlertDialog(onDismissRequest = onDismiss, confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = {
                        val selectedTransactions =
                            bulkUpdateTransactions.filter { bulkUpdateSelected.contains(it.id) }
                        onConfirmDelete(selectedTransactions.mapNotNull { it.id }.toSet())
                    }, enabled = bulkUpdateSelected.isNotEmpty()
                ) {
                    Text(
                        "Delete ${bulkUpdateSelected.size} Transaction${if (bulkUpdateSelected.size == 1) "" else "s"}",
                        color = if (bulkUpdateSelected.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
                TextButton(
                    onClick = {
                        val selectedTransactions =
                            bulkUpdateTransactions.filter { bulkUpdateSelected.contains(it.id) }
                        if (selectedTransactions.size == bulkUpdateTransactions.size) {
                            bulkUpdateSelected = emptySet()
                        } else {
                            bulkUpdateSelected = bulkUpdateTransactions.mapNotNull { it.id }.toSet()
                        }
                    }) {
                    Text(
                        if (bulkUpdateTransactions.filter { bulkUpdateSelected.contains(it.id) }.size == bulkUpdateTransactions.size) "Deselect All" else "Select All",
                        fontSize = 12.sp,
                        color = Color(0xFF00D4AA)
                    )
                }
            }
        }, dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDeleteCurrentOnly) {
                    Text("Delete current only")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }, title = {
            Text(
                "Bulk Delete Similar Transactions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }, text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 480.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Update details summary
                Card(
                    modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Update Details",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Place:", fontWeight = FontWeight.Medium, fontSize = 12.sp)
                            Text(
                                originalPlace.ifEmpty { "No Place" },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Category:", fontWeight = FontWeight.Medium, fontSize = 12.sp)
                            Text(
                                originalCategory.displayName,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                MonthYearSelectors(
                    selectedMonth = selectedMonth,
                    onMonthSelected = { selectedMonth = it },
                    selectedYear = selectedYear,
                    onYearSelected = { selectedYear = it },
                    months = months,
                    years = years
                )

                // Transaction list with scroll and selection
                if (isLoadingTransactions) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (bulkUpdateTransactions.isEmpty()) {
                    Text(
                        "No similar transactions found for ${months.find { it.first == selectedMonth }?.second} $selectedYear",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .heightIn(max = 280.dp)
                            .fillMaxWidth()
                    ) {
                        items(items = bulkUpdateTransactions, key = {
                            it.id ?: 0L
                        } // Use 0L or some stable fallback if id is null
                        ) { txn ->
                            val isSelected = bulkUpdateSelected.contains(txn.id)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        val newSelected = if (isSelected) {
                                            bulkUpdateSelected - txn.id!!
                                        } else {
                                            bulkUpdateSelected + txn.id!!
                                        }
                                        bulkUpdateSelected = newSelected
                                    }, colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(
                                        alpha = 0.3f
                                    )
                                    else MaterialTheme.colorScheme.surface
                                ), elevation = CardDefaults.cardElevation(
                                    defaultElevation = if (isSelected) 4.dp else 1.dp
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected, onCheckedChange = { checked ->
                                            txn.id?.let {
                                                bulkUpdateSelected =
                                                    if (checked) bulkUpdateSelected + it else bulkUpdateSelected - it
                                            }
                                        })
                                    Spacer(Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = txn.bankName.ifEmpty { "Unknown Bank" },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = txn.place.ifEmpty { "No Place" },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.7f
                                            )
                                        )
                                    }
                                    Column(
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            text = "₹${String.format("%.2f", txn.amount)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (txn.txnType == TransactionType.DEBIT) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = SimpleDateFormat(
                                                "dd MMM", Locale.getDefault()
                                            ).format(Date(txn.txnDate)),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.5f
                                            )
                                        )
//                                        Text(
//                                            text = txn.txnType.name,
//                                            style = MaterialTheme.typography.labelSmall,
//                                            color = if (txn.txnType == TransactionType.DEBIT) MaterialTheme.colorScheme.error.copy(
//                                                alpha = 0.7f
//                                            )
//                                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
//                                            modifier = Modifier
//                                                .background(
//                                                    color = if (txn.txnType == TransactionType.DEBIT) MaterialTheme.colorScheme.error.copy(
//                                                        alpha = 0.1f
//                                                    )
//                                                    else MaterialTheme.colorScheme.primary.copy(
//                                                        alpha = 0.1f
//                                                    ), shape = RoundedCornerShape(4.dp)
//                                                )
//                                                .padding(horizontal = 6.dp, vertical = 2.dp)
//                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        })
    }
}
